public final class IntArrayParser {

    private static final String EMPTY_ARRAY = "[]";
    private static final String DELIMITER = ",";
    private static final int START_INDEX = 1;
    
    private IntArrayParser() {
        // Prevent instantiation
    }

    /**
     * Parses a string representation of an integer array (e.g., "[1,2,3]") into an int[].
     * 
     * @param input the string to parse
     * @return the parsed int array
     * @throws IllegalArgumentException if the input is null or not properly formatted
     */
    public static int[] parse(String input) {
        validateInput(input);
        if (isEmptyArray(input)) {
            return new int[0];
        }
        String cleaned = removeBrackets(input);
        String[] elements = splitElements(cleaned);
        return parseElements(elements);
    }

    private static void validateInput(String input) {
        if (input == null || !input.startsWith("[") || !input.endsWith("]")) {
            throw new IllegalArgumentException("Input must be a non-null string enclosed in brackets, e.g., \"[1,2,3]\"");
        }
    }

    private static boolean isEmptyArray(String input) {
        return EMPTY_ARRAY.equals(input.trim());
    }

    private static String removeBrackets(String input) {
        return input.substring(START_INDEX, input.length() - 1).trim();
    }

    private static String[] splitElements(String cleaned) {
        if (cleaned.isEmpty()) {
            return new String[0];
        }
        return cleaned.split(DELIMITER);
    }

    private static int[] parseElements(String[] elements) {
        int[] result = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            result[i] = Integer.parseInt(elements[i].trim());
        }
        return result;
    }
}
```

---

**Usage Example:**

```java
int[] arr = IntArrayParser.parse("[1, 2, 3]");