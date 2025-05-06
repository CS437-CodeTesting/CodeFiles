import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Instant;

/**
 * Utility class for timestamp-keyed wrapping of elements.
 */
public final class TimestampKeyed {

    private TimestampKeyed() {
        // Prevent instantiation
    }

    /**
     * Returns a transform that maps each element to a KV of its timestamp (in millis) and the element itself.
     */
    public static <T> MapElements<T, KV<Long, T>> of() {
        return MapElements.into(
                TypeDescriptors.kvs(TypeDescriptors.longs(), new TypeDescriptor<T>() {}))
            .via((T element, org.apache.beam.sdk.transforms.MapElements.Context c) ->
                KV.of(c.timestamp().getMillis(), element));
    }
}
```

**Usage Example:**
```java
PCollection<T> input = ...;
PCollection<KV<Long, T>> output = input.apply(TimestampKeyed.of());