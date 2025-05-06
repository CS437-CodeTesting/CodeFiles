public class MessageService {

    private static final String MASKED_PASSWORD = "****";

    private final AddressInfo addressInfo;
    private final Server server;

    public MessageService(AddressInfo addressInfo, Server server) {
        this.addressInfo = addressInfo;
        this.server = server;
    }

    /**
     * Sends a message with the given request parameters.
     *
     * @param request the message request encapsulating all parameters
     * @return the result of sending the message
     * @throws IllegalStateException if sending fails
     */
    public String sendMessage(MessageRequest request) {
        auditSendMessage(request);
        return doSendMessage(request);
    }

    /**
     * Audits the sendMessage operation if auditing is enabled.
     */
    private void auditSendMessage(MessageRequest request) {
        if (AuditLogger.isEnabled()) {
            AuditLogger.sendMessage(
                this,
                null,
                request.getHeaders(),
                request.getType(),
                request.getBody(),
                request.isDurable(),
                request.getUser(),
                MASKED_PASSWORD
            );
        }
    }

    /**
     * Performs the actual message sending and handles exceptions.
     */
    private String doSendMessage(MessageRequest request) {
        try {
            return sendMessageInternal(
                addressInfo.getName(),
                server,
                request
            );
        } catch (Exception e) {
            // Consider using a logger instead of printStackTrace in production
            // Logger.error("Failed to send message", e);
            throw new IllegalStateException("Failed to send message: " + e.getMessage(), e);
        }
    }

    /**
     * Internal method to send the message.
     * (Assumes this is an existing method or replace with actual implementation)
     */
    private String sendMessageInternal(String addressName, Server server, MessageRequest request) throws Exception {
        // Actual implementation here
        // Example:
        return server.send(
            addressName,
            request.getHeaders(),
            request.getType(),
            request.getBody(),
            request.isDurable(),
            request.getUser(),
            request.getPassword()
        );
    }

    // Value object to encapsulate message parameters
    public static class MessageRequest {
        private final Map<String, String> headers;
        private final int type;
        private final String body;
        private final boolean durable;
        private final String user;
        private final String password;

        public MessageRequest(Map<String, String> headers, int type, String body, boolean durable, String user, String password) {
            this.headers = headers;
            this.type = type;
            this.body = body;
            this.durable = durable;
            this.user = user;
            this.password = password;
        }

        public Map<String, String> getHeaders() { return headers; }
        public int getType() { return type; }
        public String getBody() { return body; }
        public boolean isDurable() { return durable; }
        public String getUser() { return user; }
        public String getPassword() { return password; }
    }
}