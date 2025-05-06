import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;

/**
 * Immutable, type-safe wrapper for Avro-compatible data with JSON serialization support.
 * 
 * @param <T> the type of the datum, must be JSON-serializable and immutable
 */
public final class AvroWrapper<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final T datum;

    /**
     * Constructs an AvroWrapper with the given datum.
     * 
     * @param datum the datum to wrap; must be immutable and JSON-serializable
     * @throws NullPointerException if datum is null
     */
    public AvroWrapper(T datum) {
        this.datum = Objects.requireNonNull(datum, "datum must not be null");
    }

    /**
     * Returns the wrapped datum.
     * 
     * @return the wrapped datum
     */
    public T getDatum() {
        return datum;
    }

    /**
     * Serializes the wrapped datum to a JSON string.
     * 
     * @return JSON representation of the datum
     * @throws IllegalStateException if serialization fails
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(datum);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize datum to JSON", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AvroWrapper<?>)) return false;
        AvroWrapper<?> other = (AvroWrapper<?>) obj;
        return Objects.equals(this.datum, other.datum);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(datum);
    }

    @Override
    public String toString() {
        return "AvroWrapper{" + "datum=" + datum + '}';
    }
}