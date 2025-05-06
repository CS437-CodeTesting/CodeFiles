// Utility class for converting TableReferenceProto.TableReference to table spec String
public final class TableReferenceConverter {

    private TableReferenceConverter() {
        // Prevent instantiation
    }

    /**
     * Converts a TableReferenceProto.TableReference to its table spec String representation.
     *
     * @param tableReference the TableReferenceProto.TableReference to convert
     * @return the table spec String
     * @throws NullPointerException if tableReference is null
     */
    public static String toTableSpec(TableReferenceProto.TableReference tableReference) {
        if (tableReference == null) {
            throw new NullPointerException("tableReference must not be null");
        }
        // Implement the actual conversion logic here.
        // Example (replace with actual logic as needed):
        return String.format("%s:%s.%s",
                tableReference.getProjectId(),
                tableReference.getDatasetId(),
                tableReference.getTableId());
    }
}
```

**How this addresses the issues:**

- **Indirection:** The conversion is now a direct static method call, not a pass-through via an adapter.
- **Abstraction Failure:** The utility class clearly encapsulates the conversion logic, making its purpose explicit.
- **SRP Violation:** The class has a single responsibility: converting `TableReferenceProto.TableReference` to a table spec string.
- **Poltergeists:** No transient, stateless function objects are created; the logic is centralized and reusable.

**Usage Example:**
```java
String tableSpec = TableReferenceConverter.toTableSpec(tableReference);