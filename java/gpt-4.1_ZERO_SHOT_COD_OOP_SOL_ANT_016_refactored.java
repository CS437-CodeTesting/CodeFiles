import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.function.Function;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the mapping of parameter fields to their string keys and value extractors.
 */
public class PagingParameters {

    private Integer limit;
    private Integer offset;
    private Long since;
    private Long until;
    private String after;
    private String before;
    private String pagingToken;

    // Parameter name constants
    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_SINCE = "since";
    private static final String PARAM_UNTIL = "until";
    private static final String PARAM_AFTER = "after";
    private static final String PARAM_BEFORE = "before";
    private static final String PARAM_PAGING_TOKEN = "__paging_token";

    // Parameter descriptor abstraction
    private static class ParamDescriptor<T> {
        final String key;
        final Function<PagingParameters, T> valueExtractor;
        final Function<T, String> toStringFunc;

        ParamDescriptor(String key, Function<PagingParameters, T> valueExtractor, Function<T, String> toStringFunc) {
            this.key = key;
            this.valueExtractor = valueExtractor;
            this.toStringFunc = toStringFunc;
        }

        String extractString(PagingParameters params) {
            T value = valueExtractor.apply(params);
            return value != null ? toStringFunc.apply(value) : null;
        }
    }

    // List of all parameter descriptors
    private static final List<ParamDescriptor<?>> PARAM_DESCRIPTORS = Arrays.asList(
        new ParamDescriptor<>(PARAM_LIMIT,       p -> p.limit,        v -> String.valueOf(v)),
        new ParamDescriptor<>(PARAM_OFFSET,      p -> p.offset,       v -> String.valueOf(v)),
        new ParamDescriptor<>(PARAM_SINCE,       p -> p.since,        v -> String.valueOf(v)),
        new ParamDescriptor<>(PARAM_UNTIL,       p -> p.until,        v -> String.valueOf(v)),
        new ParamDescriptor<>(PARAM_AFTER,       p -> p.after,        v -> v),
        new ParamDescriptor<>(PARAM_BEFORE,      p -> p.before,       v -> v),
        new ParamDescriptor<>(PARAM_PAGING_TOKEN,p -> p.pagingToken,  v -> v)
    );

    /**
     * Converts the parameters to a MultiValueMap, omitting nulls.
     */
    public MultiValueMap<String, String> toMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (ParamDescriptor<?> descriptor : PARAM_DESCRIPTORS) {
            String value = descriptor.extractString(this);
            if (value != null) {
                map.set(descriptor.key, value);
            }
        }
        return map;
    }

    // Getters and setters omitted for brevity
    // ...
}