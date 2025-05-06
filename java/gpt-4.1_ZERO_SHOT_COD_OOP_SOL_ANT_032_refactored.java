import java.io.File;
import java.util.Objects;

/**
 * Immutable configuration for assembling a portlet web application archive (WAR).
 * Encapsulates all configuration and validation logic.
 */
public final class AssemblerConfig {

    private static final int DEFAULT_ASSEMBLER_SINK_BUFLEN = 4096;
    private static final int MIN_ASSEMBLER_SINK_BUFLEN = 1024;

    private final File portletDescriptor;
    private final File webappDescriptor;
    private final File destination;
    private final String dispatchServletClass;
    private final File source;
    private final int assemblerSinkBuflen;

    private AssemblerConfig(Builder builder) {
        this.portletDescriptor = builder.portletDescriptor;
        this.webappDescriptor = builder.webappDescriptor;
        this.destination = builder.destination;
        this.dispatchServletClass = builder.dispatchServletClass;
        this.source = builder.source;
        this.assemblerSinkBuflen = builder.assemblerSinkBuflen;
        validate();
    }

    /**
     * Validates the configuration. Throws IllegalArgumentException if invalid.
     */
    private void validate() {
        if (portletDescriptor == null || !portletDescriptor.isFile()) {
            throw new IllegalArgumentException("Portlet descriptor must be a valid file.");
        }
        if (webappDescriptor == null || !webappDescriptor.isFile()) {
            throw new IllegalArgumentException("Webapp descriptor must be a valid file.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination file must not be null.");
        }
        if (dispatchServletClass == null || dispatchServletClass.trim().isEmpty()) {
            throw new IllegalArgumentException("Dispatch servlet class must not be null or empty.");
        }
        if (source == null || !source.exists()) {
            throw new IllegalArgumentException("Source archive must exist.");
        }
        if (assemblerSinkBuflen < MIN_ASSEMBLER_SINK_BUFLEN) {
            throw new IllegalArgumentException("Assembler sink buffer size must be at least " + MIN_ASSEMBLER_SINK_BUFLEN + " bytes.");
        }
    }

    // Getters (no setters, immutable)
    public File getPortletDescriptor() {
        return portletDescriptor;
    }

    public File getWebappDescriptor() {
        return webappDescriptor;
    }

    public File getDestination() {
        return destination;
    }

    public String getDispatchServletClass() {
        return dispatchServletClass;
    }

    public File getSource() {
        return source;
    }

    public int getAssemblerSinkBuflen() {
        return assemblerSinkBuflen;
    }

    /**
     * Builder for AssemblerConfig.
     */
    public static class Builder {
        private File portletDescriptor;
        private File webappDescriptor;
        private File destination;
        private String dispatchServletClass;
        private File source;
        private int assemblerSinkBuflen = DEFAULT_ASSEMBLER_SINK_BUFLEN;

        public Builder portletDescriptor(File portletDescriptor) {
            this.portletDescriptor = portletDescriptor;
            return this;
        }

        public Builder webappDescriptor(File webappDescriptor) {
            this.webappDescriptor = webappDescriptor;
            return this;
        }

        public Builder destination(File destination) {
            this.destination = destination;
            return this;
        }

        public Builder dispatchServletClass(String dispatchServletClass) {
            this.dispatchServletClass = dispatchServletClass;
            return this;
        }

        public Builder source(File source) {
            this.source = source;
            return this;
        }

        public Builder assemblerSinkBuflen(int assemblerSinkBuflen) {
            this.assemblerSinkBuflen = assemblerSinkBuflen;
            return this;
        }

        public AssemblerConfig build() {
            return new AssemblerConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssemblerConfig)) return false;
        AssemblerConfig that = (AssemblerConfig) o;
        return assemblerSinkBuflen == that.assemblerSinkBuflen &&
                Objects.equals(portletDescriptor, that.portletDescriptor) &&
                Objects.equals(webappDescriptor, that.webappDescriptor) &&
                Objects.equals(destination, that.destination) &&
                Objects.equals(dispatchServletClass, that.dispatchServletClass) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(portletDescriptor, webappDescriptor, destination, dispatchServletClass, source, assemblerSinkBuflen);
    }

    @Override
    public String toString() {
        return "AssemblerConfig{" +
                "portletDescriptor=" + portletDescriptor +
                ", webappDescriptor=" + webappDescriptor +
                ", destination=" + destination +
                ", dispatchServletClass='" + dispatchServletClass + '\'' +
                ", source=" + source +
                ", assemblerSinkBuflen=" + assemblerSinkBuflen +
                '}';
    }
}