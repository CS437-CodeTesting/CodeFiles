// Interface for transformation responsibility
public interface KeyReferenceTransformable {
    void transformKeyReferences(RefTransformer visitor);
}

// Example Config class implementing the interface
public class Config implements KeyReferenceTransformable {
    // Config fields and methods...

    @Override
    public void transformKeyReferences(RefTransformer visitor) {
        // Actual transformation logic for this config
        // e.g., this.key = visitor.transform(this.key);
    }
}

// The container class now delegates transformation to its configs
public class ConfigManager {
    private final List<KeyReferenceTransformable> configs;

    public ConfigManager(List<KeyReferenceTransformable> configs) {
        this.configs = new ArrayList<>(configs);
    }

    // Now, the manager only coordinates, not transforms
    public void transformAllKeyReferences(RefTransformer visitor) {
        for (KeyReferenceTransformable config : configs) {
            config.transformKeyReferences(visitor);
        }
    }

    // Other ConfigManager methods...
}

// Example usage
public class RefTransformer {
    // Transformation logic...
    // public String transform(String key) { ... }
}