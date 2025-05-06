import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a response containing application instances.
 * Immutable, validated, and serialization-friendly.
 */
@JsonDeserialize(using = ApplicationInstancesResponseDeserializer.class)
public final class ApplicationInstancesResponse {

    private final Map<String, ApplicationInstanceInfo> instances;

    /**
     * Constructs an ApplicationInstancesResponse.
     * @param instances Map of instance IDs to ApplicationInstanceInfo. Never null.
     */
    @JsonCreator
    public ApplicationInstancesResponse(
            @JsonProperty("instances") Map<String, ApplicationInstanceInfo> instances) {
        // Defensive copy and null handling
        this.instances = instances == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(instances);
    }

    /**
     * Returns the application instances.
     * @return Unmodifiable map of instances. Never null.
     */
    @JsonProperty("instances")
    public Map<String, ApplicationInstanceInfo> getInstances() {
        return instances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationInstancesResponse)) return false;
        ApplicationInstancesResponse that = (ApplicationInstancesResponse) o;
        return Objects.equals(instances, that.instances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instances);
    }

    @Override
    public String toString() {
        return "ApplicationInstancesResponse{" +
                "instances=" + instances +
                '}';
    }
}

// --- Dedicated deserializer, if custom logic is needed ---

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;

/**
 * Custom deserializer for ApplicationInstancesResponse.
 * Handles nulls and ensures immutability.
 */
public class ApplicationInstancesResponseDeserializer extends JsonDeserializer<ApplicationInstancesResponse> {

    @Override
    public ApplicationInstancesResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, ApplicationInstanceInfo> instances = p.readValueAs(
                new TypeReference<Map<String, ApplicationInstanceInfo>>() {});
        return new ApplicationInstancesResponse(instances);
    }
}

// --- Example ApplicationInstanceInfo class (for completeness) ---

public final class ApplicationInstanceInfo {
    // Implementation details...
}