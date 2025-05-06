import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Encapsulates the required alert properties for the alert system.
 * This class provides an immutable set of required property names,
 * avoiding magic strings and exposing only necessary information.
 */
public final class RequiredAlertProperties {

    // Define property names as constants to avoid magic strings
    public static final String ALERT_STATE = "AlertState";
    public static final String ALERT_ORIGINAL_TIMESTAMP = "AlertOriginalTimestamp";
    public static final String ALERT_MAINTENANCE_STATE = "AlertMaintenanceState";

    // Immutable set of required properties
    private static final Set<String> REQUIRED_PROPERTIES;

    static {
        Set<String> props = new LinkedHashSet<>();
        props.add(ALERT_STATE);
        props.add(ALERT_ORIGINAL_TIMESTAMP);
        props.add(ALERT_MAINTENANCE_STATE);
        REQUIRED_PROPERTIES = Collections.unmodifiableSet(props);
    }

    // Private constructor to prevent instantiation
    private RequiredAlertProperties() {}

    /**
     * Returns an immutable set of required alert property names.
     *
     * @return unmodifiable set of required property names
     */
    public static Set<String> getRequiredProperties() {
        return REQUIRED_PROPERTIES;
    }
}
```

**Usage Example:**
```java
// Instead of passing around and mutating sets, simply use:
Set<String> requiredProps = RequiredAlertProperties.getRequiredProperties();