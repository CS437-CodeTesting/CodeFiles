import java.util.Objects;
import java.util.Optional;

// --- Encoding Strategy Enum ---
public enum EncodingStrategy {
    DEFAULT,
    OVERRIDE_EXPLICIT,
    CONVERT_EXPLICIT
}

// --- FileEncoding (Stub) ---
public enum FileEncoding {
    AUTOMATICALLY_DETECT,
    UTF8,
    UTF16,
    ASCII
    // ... add as needed
}

// --- PropertyValue (Stub) ---
public class PropertyValue {
    private final String name;
    private final String value;

    public PropertyValue(String name, String value) {
        this.name = Objects.requireNonNull(name, "Property name cannot be null");
        this.value = value;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
}

// --- Encoding Resolution Options ---
public final class EncodingResolutionOptions {
    private final EncodingStrategy strategy;
    private final FileEncoding explicitEncoding;

    public EncodingResolutionOptions(EncodingStrategy strategy, FileEncoding explicitEncoding) {
        if (strategy == null) throw new IllegalArgumentException("EncodingStrategy cannot be null");
        if (strategy == EncodingStrategy.DEFAULT && explicitEncoding != null) {
            throw new IllegalArgumentException("explicitEncoding must be null if strategy is DEFAULT");
        }
        if (strategy != EncodingStrategy.DEFAULT && explicitEncoding == null) {
            throw new IllegalArgumentException("explicitEncoding must be non-null if strategy is not DEFAULT");
        }
        this.strategy = strategy;
        this.explicitEncoding = explicitEncoding;
    }

    public EncodingStrategy getStrategy() { return strategy; }
    public Optional<FileEncoding> getExplicitEncoding() { return Optional.ofNullable(explicitEncoding); }
}

// --- Merge Resolution Options ---
public final class MergeResolutionOptions {
    private final boolean useInternalEngine;
    private final boolean acceptMergeWithConflicts;
    private final FileEncoding acceptMergeEncoding;

    public MergeResolutionOptions(boolean useInternalEngine, boolean acceptMergeWithConflicts, FileEncoding acceptMergeEncoding) {
        this.useInternalEngine = useInternalEngine;
        this.acceptMergeWithConflicts = acceptMergeWithConflicts;
        this.acceptMergeEncoding = acceptMergeEncoding;
    }

    public boolean isUseInternalEngine() { return useInternalEngine; }
    public boolean isAcceptMergeWithConflicts() { return acceptMergeWithConflicts; }
    public Optional<FileEncoding> getAcceptMergeEncoding() { return Optional.ofNullable(acceptMergeEncoding); }
}

// --- Path Resolution Options ---
public final class PathResolutionOptions {
    private final String newPath;

    public PathResolutionOptions(String newPath) {
        this.newPath = newPath;
    }

    public Optional<String> getNewPath() { return Optional.ofNullable(newPath); }
}

// --- Property Resolution Options ---
public final class PropertyResolutionOptions {
    private final PropertyValue[] acceptMergeProperties;

    public PropertyResolutionOptions(PropertyValue[] acceptMergeProperties) {
        this.acceptMergeProperties = acceptMergeProperties != null ? acceptMergeProperties.clone() : null;
    }

    public Optional<PropertyValue[]> getAcceptMergeProperties() {
        return acceptMergeProperties == null ? Optional.empty() : Optional.of(acceptMergeProperties.clone());
    }
}

// --- Main ResolutionOptions Class ---
public final class ResolutionOptions {
    private final EncodingResolutionOptions encodingOptions;
    private final MergeResolutionOptions mergeOptions;
    private final PathResolutionOptions pathOptions;
    private final PropertyResolutionOptions propertyOptions;

    private ResolutionOptions(Builder builder) {
        this.encodingOptions = builder.encodingOptions;
        this.mergeOptions = builder.mergeOptions;
        this.pathOptions = builder.pathOptions;
        this.propertyOptions = builder.propertyOptions;
    }

    public EncodingResolutionOptions getEncodingOptions() { return encodingOptions; }
    public MergeResolutionOptions getMergeOptions() { return mergeOptions; }
    public PathResolutionOptions getPathOptions() { return pathOptions; }
    public PropertyResolutionOptions getPropertyOptions() { return propertyOptions; }

    // --- Builder Pattern for Construction ---
    public static class Builder {
        private EncodingResolutionOptions encodingOptions = new EncodingResolutionOptions(EncodingStrategy.DEFAULT, null);
        private MergeResolutionOptions mergeOptions = new MergeResolutionOptions(true, false, null);
        private PathResolutionOptions pathOptions = new PathResolutionOptions(null);
        private PropertyResolutionOptions propertyOptions = new PropertyResolutionOptions(null);

        public Builder encodingOptions(EncodingResolutionOptions encodingOptions) {
            this.encodingOptions = Objects.requireNonNull(encodingOptions);
            return this;
        }

        public Builder mergeOptions(MergeResolutionOptions mergeOptions) {
            this.mergeOptions = Objects.requireNonNull(mergeOptions);
            return this;
        }

        public Builder pathOptions(PathResolutionOptions pathOptions) {
            this.pathOptions = Objects.requireNonNull(pathOptions);
            return this;
        }

        public Builder propertyOptions(PropertyResolutionOptions propertyOptions) {
            this.propertyOptions = Objects.requireNonNull(propertyOptions);
            return this;
        }

        public ResolutionOptions build() {
            return new ResolutionOptions(this);
        }
    }

    // --- Factory method for default options ---
    public static ResolutionOptions defaultOptions() {
        return new Builder().build();
    }
}
```

---

**Key Improvements:**

- **Enums** replace custom typesafe enums.
- **Immutability**: All option classes are immutable, ensuring thread safety and correctness.
- **Single Responsibility**: Each class encapsulates a distinct aspect of resolution options.
- **Validation**: Constructors enforce invariants.
- **Builder Pattern**: For flexible, readable construction of `ResolutionOptions`.
- **No inheritance abuse**: Only uses inheritance where appropriate (e.g., enums).
- **Production-ready**: All fields are final, null-safety is enforced, and code is clean and maintainable.

---

**Usage Example:**

```java
ResolutionOptions options = new ResolutionOptions.Builder()
    .encodingOptions(new EncodingResolutionOptions(EncodingStrategy.CONVERT_EXPLICIT, FileEncoding.UTF8))
    .mergeOptions(new MergeResolutionOptions(true, true, FileEncoding.UTF16))
    .pathOptions(new PathResolutionOptions("/new/path/file.txt"))
    .propertyOptions(new PropertyResolutionOptions(new PropertyValue[] {
        new PropertyValue("author", "Alice")
    }))
    .build();