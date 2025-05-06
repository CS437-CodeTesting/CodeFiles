// Package and imports omitted for brevity

@StatsDoc(
    name = BookKeeperClientStats.CHANNEL_SCOPE,
    help = "Per channel bookie client stats"
)
public final class PerChannelBookieClient extends ChannelInboundHandlerAdapter {

    // --- Static Constants and Utilities ---

    private static final Logger LOG = LoggerFactory.getLogger(PerChannelBookieClient.class);

    private static final Set<Integer> EXPECTED_BK_OPERATION_ERRORS = Collections.unmodifiableSet(Sets
            .newHashSet(
                BKException.Code.BookieHandleNotAvailableException,
                BKException.Code.NoSuchEntryException,
                BKException.Code.NoSuchLedgerExistsException,
                BKException.Code.LedgerFencedException,
                BKException.Code.LedgerExistException,
                BKException.Code.DuplicateEntryIdException,
                BKException.Code.WriteOnReadOnlyBookieException
            ));

    private static final int DEFAULT_HIGH_PRIORITY_VALUE = 100;
    private static final AtomicLong TXN_ID_GENERATOR = new AtomicLong(0);

    // --- Dependencies and Configuration ---

    private final BookieSocketAddress addr;
    private final EventLoopGroup eventLoopGroup;
    private final ByteBufAllocator allocator;
    private final OrderedExecutor executor;
    private final long addEntryTimeoutNanos;
    private final long readEntryTimeoutNanos;
    private final int maxFrameSize;
    private final int getBookieInfoTimeout;
    private final int startTLSTimeout;
    private final boolean useV2WireProtocol;
    private final boolean preserveMdcForTaskExecution;
    private final ClientConfiguration conf;
    private final PerChannelBookieClientPool pcbcPool;
    private final ClientAuthProvider.Factory authProviderFactory;
    private final ExtensionRegistry extRegistry;
    private final SecurityHandlerFactory shFactory;

    // --- State ---

    private final CompletionRegistry completionRegistry;
    private final PendingOpsQueue pendingOpsQueue;
    private final ConnectionStateManager connectionStateManager;
    private final ChannelStats channelStats;
    private final ClientConnectionPeer connectionPeer;

    private volatile Channel channel = null;
    private volatile BookKeeperPrincipal authorizedId = BookKeeperPrincipal.ANONYMOUS;
    private volatile boolean isWritable = true;

    // --- Constructor ---

    public PerChannelBookieClient(
            ClientConfiguration conf,
            OrderedExecutor executor,
            EventLoopGroup eventLoopGroup,
            ByteBufAllocator allocator,
            BookieSocketAddress addr,
            StatsLogger parentStatsLogger,
            ClientAuthProvider.Factory authProviderFactory,
            ExtensionRegistry extRegistry,
            PerChannelBookieClientPool pcbcPool,
            SecurityHandlerFactory shFactory) throws SecurityException {

        this.conf = conf;
        this.executor = executor;
        this.addr = addr;
        this.allocator = allocator;
        this.pcbcPool = pcbcPool;
        this.authProviderFactory = authProviderFactory;
        this.extRegistry = extRegistry;
        this.shFactory = shFactory;

        this.maxFrameSize = conf.getNettyMaxFrameSizeBytes();
        this.addEntryTimeoutNanos = TimeUnit.SECONDS.toNanos(conf.getAddEntryTimeout());
        this.readEntryTimeoutNanos = TimeUnit.SECONDS.toNanos(conf.getReadEntryTimeout());
        this.getBookieInfoTimeout = conf.getBookieInfoTimeout();
        this.startTLSTimeout = conf.getStartTLSTimeout();
        this.useV2WireProtocol = conf.getUseV2WireProtocol();
        this.preserveMdcForTaskExecution = conf.getPreserveMdcForTaskExecution();

        this.eventLoopGroup = LocalBookiesRegistry.isLocalBookie(addr)
                ? new DefaultEventLoopGroup()
                : eventLoopGroup;

        if (shFactory != null) {
            shFactory.init(NodeType.Client, conf, allocator);
        }

        this.channelStats = new ChannelStats(parentStatsLogger, addr);
        this.completionRegistry = new CompletionRegistry(addEntryTimeoutNanos, readEntryTimeoutNanos, executor, channelStats, addr);
        this.pendingOpsQueue = new PendingOpsQueue();
        this.connectionStateManager = new ConnectionStateManager();

        this.connectionPeer = new DefaultClientConnectionPeer();
    }

    // --- Public API ---

    public boolean isWritable() {
        return isWritable;
    }

    public void setWritable(boolean val) {
        isWritable = val;
    }

    public void connectIfNeededAndDoOp(GenericCallback<PerChannelBookieClient> op) {
        if (connectionStateManager.isConnected(channel)) {
            completeOperation(op, BKException.Code.OK);
            return;
        }

        synchronized (this) {
            if (connectionStateManager.isConnected(channel)) {
                completeOperation(op, BKException.Code.OK);
            } else if (connectionStateManager.isClosed()) {
                completeOperation(op, BKException.Code.BookieHandleNotAvailableException);
            } else {
                pendingOpsQueue.add(op);
                if (connectionStateManager.isConnectingOrTls()) {
                    return;
                }
                connectionStateManager.setConnecting();
                connect();
            }
        }
    }

    public void disconnect() {
        disconnect(true);
    }

    public void disconnect(boolean wait) {
        LOG.info("Disconnecting the per channel bookie client for {}", addr);
        closeInternal(false, wait);
    }

    public void close() {
        close(true);
    }

    public void close(boolean wait) {
        LOG.info("Closing the per channel bookie client for {}", addr);
        connectionStateManager.close();
        completionRegistry.errorOutOutstandingEntries(BKException.Code.ClientClosedException);
        channelStats.decrementActiveChannel(channel);
        closeInternal(true, wait);
    }

    public void checkTimeoutOnPendingOperations() {
        int timedOutOperations = completionRegistry.removeIfTimeout();
        if (timedOutOperations > 0) {
            LOG.info("Timed-out {} operations to channel {} for {}",
                    timedOutOperations, channel, addr);
        }
    }

    // --- Channel/Netty Event Handlers ---

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Disconnected from bookie channel {}", ctx.channel());
        channelStats.decrementActiveChannel(ctx.channel());
        completionRegistry.errorOutOutstandingEntries(BKException.Code.BookieHandleNotAvailableException);
        pendingOpsQueue.errorOutPendingOps(BKException.Code.BookieHandleNotAvailableException, this);
        connectionStateManager.onChannelInactive(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        channelStats.incrementExceptionCounter();
        if (cause instanceof CorruptedFrameException || cause instanceof TooLongFrameException) {
            LOG.error("Corrupted frame received from bookie: {}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }
        if (cause instanceof AuthHandler.AuthenticationException) {
            LOG.error("Error authenticating connection", cause);
            completionRegistry.errorOutOutstandingEntries(BKException.Code.UnauthorizedAccessException);
            closeChannel(ctx.channel());
            return;
        }
        if (cause instanceof DecoderException && cause.getCause() instanceof SSLHandshakeException) {
            LOG.error("TLS handshake failed", cause);
            pendingOpsQueue.errorOutPendingOps(BKException.Code.SecurityException, this);
            closeChannel(ctx.channel());
            return;
        }
        if (cause