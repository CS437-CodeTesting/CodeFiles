import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

// --- Interfaces ---

public interface Message {
    MessageId getMessageId();
    Date getInternalDate();
    String getMediaType();
    String getSubType();
    long getBodyOctets();
    long getHeaderOctets();
    long getFullContentOctets();
    Long getTextualLineCount();
    InputStream getBodyContent() throws IOException;
    InputStream getHeaderContent() throws IOException;
    InputStream getFullContent() throws IOException;
    List<Property> getProperties();
    List<MessageAttachment> getAttachments();
}

// --- Value Objects ---

public final class MessageMetadata {
    private final MessageId messageId;
    private final Date internalDate;
    private final String mediaType;
    private final String subType;
    private final Long textualLineCount;

    public MessageMetadata(MessageId messageId, Date internalDate, String mediaType, String subType, Long textualLineCount) {
        this.messageId = Objects.requireNonNull(messageId, "messageId must not be null");
        this.internalDate = new Date(Objects.requireNonNull(internalDate, "internalDate must not be null").getTime());
        this.mediaType = Objects.requireNonNull(mediaType, "mediaType must not be null");
        this.subType = Objects.requireNonNull(subType, "subType must not be null");
        this.textualLineCount = textualLineCount;
    }

    public MessageId getMessageId() { return messageId; }
    public Date getInternalDate() { return new Date(internalDate.getTime()); }
    public String getMediaType() { return mediaType; }
    public String getSubType() { return subType; }
    public Long getTextualLineCount() { return textualLineCount; }
}

public final class MessageContent {
    private final SharedInputStream content;
    private final int bodyStartOctet;
    private final long size;

    public MessageContent(SharedInputStream content, int bodyStartOctet, long size) {
        this.content = Objects.requireNonNull(content, "content must not be null");
        if (bodyStartOctet < 0) throw new IllegalArgumentException("bodyStartOctet must be >= 0");
        if (size < 0) throw new IllegalArgumentException("size must be >= 0");
        this.bodyStartOctet = bodyStartOctet;
        this.size = size;
    }

    public InputStream getBodyContent() throws IOException {
        return content.newStream(bodyStartOctet, -1);
    }

    public InputStream getHeaderContent() throws IOException {
        long headerEnd = Math.max(0, bodyStartOctet);
        return content.newStream(0, headerEnd);
    }

    public InputStream getFullContent() throws IOException {
        return content.newStream(0, -1);
    }

    public long getBodyOctets() {
        return size - bodyStartOctet;
    }

    public long getHeaderOctets() {
        return bodyStartOctet;
    }

    public long getFullContentOctets() {
        return size;
    }
}

public final class MessageProperties {
    private final List<Property> properties;
    private final List<MessageAttachment> attachments;

    public MessageProperties(List<Property> properties, List<MessageAttachment> attachments) {
        this.properties = properties == null ? Collections.emptyList() : Collections.unmodifiableList(properties);
        this.attachments = attachments == null ? Collections.emptyList() : Collections.unmodifiableList(attachments);
    }

    public List<Property> getProperties() { return properties; }
    public List<MessageAttachment> getAttachments() { return attachments; }
}

// --- Main Message Implementation ---

public final class SimpleMessage implements Message {
    private final MessageMetadata metadata;
    private final MessageContent content;
    private final MessageProperties messageProperties;

    public SimpleMessage(
            MessageMetadata metadata,
            MessageContent content,
            MessageProperties messageProperties
    ) {
        this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.messageProperties = Objects.requireNonNull(messageProperties, "messageProperties must not be null");
    }

    // Convenience constructor for legacy usage
    public SimpleMessage(
            MessageId messageId,
            SharedInputStream contentStream,
            long size,
            Date internalDate,
            String subType,
            String mediaType,
            int bodyStartOctet,
            Long textualLineCount,
            List<Property> properties,
            List<MessageAttachment> attachments
    ) {
        this(
            new MessageMetadata(messageId, internalDate, mediaType, subType, textualLineCount),
            new MessageContent(contentStream, bodyStartOctet, size),
            new MessageProperties(properties, attachments)
        );
    }

    public SimpleMessage(
            MessageId messageId,
            SharedInputStream contentStream,
            long size,
            Date internalDate,
            String subType,
            String mediaType,
            int bodyStartOctet,
            Long textualLineCount,
            List<Property> properties
    ) {
        this(
            new MessageMetadata(messageId, internalDate, mediaType, subType, textualLineCount),
            new MessageContent(contentStream, bodyStartOctet, size),
            new MessageProperties(properties, Collections.emptyList())
        );
    }

    @Override
    public MessageId getMessageId() {
        return metadata.getMessageId();
    }

    @Override
    public Date getInternalDate() {
        return metadata.getInternalDate();
    }

    @Override
    public String getMediaType() {
        return metadata.getMediaType();
    }

    @Override
    public String getSubType() {
        return metadata.getSubType();
    }

    @Override
    public Long getTextualLineCount() {
        return metadata.getTextualLineCount();
    }

    @Override
    public InputStream getBodyContent() throws IOException {
        return content.getBodyContent();
    }

    @Override
    public InputStream getHeaderContent() throws IOException {
        return content.getHeaderContent();
    }

    @Override
    public InputStream getFullContent() throws IOException {
        return content.getFullContent();
    }

    @Override
    public long getBodyOctets() {
        return content.getBodyOctets();
    }

    @Override
    public long getHeaderOctets() {
        return content.getHeaderOctets();
    }

    @Override
    public long getFullContentOctets() {
        return content.getFullContentOctets();
    }

    @Override
    public List<Property> getProperties() {
        return messageProperties.getProperties();
    }

    @Override
    public List<MessageAttachment> getAttachments() {
        return messageProperties.getAttachments();
    }
}

// --- Supporting Types (stubs for compilation) ---

interface MessageId {}
interface SharedInputStream {
    InputStream newStream(long start, long end) throws IOException;
}
interface Property {}
interface MessageAttachment {}