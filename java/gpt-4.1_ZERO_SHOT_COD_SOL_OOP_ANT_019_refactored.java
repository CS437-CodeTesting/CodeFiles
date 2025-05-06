import java.io.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Custom exception for configuration errors
class IllegalConfigurationException extends RuntimeException {
    public IllegalConfigurationException(String message) { super(message); }
    public IllegalConfigurationException(String message, Throwable cause) { super(message, cause); }
}

// Interface for event logging (assumed)
interface EventLoggerProvider {}

// Placeholder for RuleSet and RuleSetCreator
class RuleSet {}
class RuleSetCreator {
    void addAcl(Integer number, List<String> tokens, int line) {/*...*/}
    void addConfig(List<String> tokens, int line) {/*...*/}
    RuleSet createRuleSet(EventLoggerProvider logger) { return new RuleSet(); }
}

// Main parser class
public class RuleSetParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleSetParser.class);

    // Tokenizer configuration constants
    private static final char COMMENT = '#';
    private static final char CONTINUATION = '\\';
    private static final String ACL = "ACL";
    private static final String CONFIG = "CONFIG";
    private static final String GROUP = "GROUP";

    // Error messages
    private static final String NOT_ENOUGH_TOKENS_MSG = "Not enough tokens at line %d.";
    private static final String UNRECOGNISED_INITIAL_MSG = "Unrecognised initial token '%s' at line %d.";
    private static final String NUMBER_NOT_ALLOWED_MSG = "Number not allowed before '%s' at line %d.";
    private static final String PREMATURE_CONTINUATION_MSG = "Premature continuation at line %d.";
    private static final String PREMATURE_EOF_MSG = "Premature EOF at line %d.";
    private static final String PARSE_TOKEN_FAILED_MSG = "Failed to parse token at line %d.";
    private static final String CANNOT_LOAD_MSG = "Cannot load configuration file.";

    private final RuleSetCreator ruleSetCreator;
    private final EventLoggerProvider eventLogger;

    public RuleSetParser(RuleSetCreator ruleSetCreator, EventLoggerProvider eventLogger) {
        this.ruleSetCreator = ruleSetCreator;
        this.eventLogger = eventLogger;
    }

    public RuleSet parse(Reader configReader) {
        Objects.requireNonNull(configReader, "configReader must not be null");
        int line = 0;
        try (BufferedReader bufferedReader = new BufferedReader(configReader)) {
            LOGGER.debug("About to load ACL file");
            StreamTokenizer tokenizer = createTokenizer(bufferedReader);

            Deque<String> tokenStack = new ArrayDeque<>();
            int currentToken;
            while ((currentToken = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
                line = tokenizer.lineno() - 1;
                if (currentToken == StreamTokenizer.TT_EOL) {
                    processLine(tokenStack, line);
                    tokenStack.clear();
                } else if (currentToken == StreamTokenizer.TT_NUMBER) {
                    tokenStack.addLast(String.valueOf((int) tokenizer.nval));
                } else if (currentToken == StreamTokenizer.TT_WORD) {
                    tokenStack.addLast(tokenizer.sval);
                } else if (tokenizer.ttype == CONTINUATION) {
                    handleContinuation(tokenizer, line);
                } else if (tokenizer.ttype == '\'' || tokenizer.ttype == '"') {
                    tokenStack.addLast(tokenizer.sval);
                } else {
                    tokenStack.addLast(Character.toString((char) tokenizer.ttype));
                }
            }
            // Process any remaining tokens (last line may not end with EOL)
            if (!tokenStack.isEmpty()) {
                processLine(tokenStack, line);
            }
        } catch (IllegalArgumentException iae) {
            throw new IllegalConfigurationException(String.format(PARSE_TOKEN_FAILED_MSG, line), iae);
        } catch (IOException ioe) {
            throw new IllegalConfigurationException(CANNOT_LOAD_MSG, ioe);
        }
        return ruleSetCreator.createRuleSet(eventLogger);
    }

    private StreamTokenizer createTokenizer(Reader reader) {
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.resetSyntax();
        tokenizer.commentChar(COMMENT);
        tokenizer.eolIsSignificant(true);
        tokenizer.ordinaryChar('=');
        tokenizer.ordinaryChar(CONTINUATION);
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.whitespaceChars('\u0000', '\u0020');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('_', '_');
        tokenizer.wordChars('-', '-');
        tokenizer.wordChars('.', '.');
        tokenizer.wordChars('*', '*');
        tokenizer.wordChars('@', '@');
        tokenizer.wordChars(':', ':');
        return tokenizer;
    }

    private void processLine(Deque<String> tokenStack, int line) {
        if (tokenStack.isEmpty()) {
            return; // blank line
        }
        List<String> tokens = new ArrayList<>(tokenStack);
        Iterator<String> iterator = tokens.iterator();

        String first = nextToken(iterator, line, NOT_ENOUGH_TOKENS_MSG);
        Integer number = null;

        // Check if first token is a number
        if (first.matches("\\d+")) {
            number = Integer.valueOf(first);
            first = nextToken(iterator, line, NOT_ENOUGH_TOKENS_MSG);
        }

        if (ACL.equalsIgnoreCase(first)) {
            ruleSetCreator.addAcl(number, collectRemainingTokens(iterator), line);
        } else if (number == null) {
            if (GROUP.equalsIgnoreCase(first)) {
                throw new IllegalConfigurationException(String.format(
                        "GROUP keyword not supported at line %d. Groups should be defined via a Group Provider, not in the ACL file.", line));
            } else if (CONFIG.equalsIgnoreCase(first)) {
                ruleSetCreator.addConfig(collectRemainingTokens(iterator), line);
            } else {
                throw new IllegalConfigurationException(String.format(UNRECOGNISED_INITIAL_MSG, first, line));
            }
        } else {
            throw new IllegalConfigurationException(String.format(NUMBER_NOT_ALLOWED_MSG, first, line));
        }
    }

    private String nextToken(Iterator<String> iterator, int line, String errorMsg) {
        if (!iterator.hasNext()) {
            throw new IllegalConfigurationException(String.format(errorMsg, line));
        }
        return iterator.next();
    }

    private List<String> collectRemainingTokens(Iterator<String> iterator) {
        List<String> remaining = new ArrayList<>();
        while (iterator.hasNext()) {
            remaining.add(iterator.next());
        }
        return remaining;
    }

    private void handleContinuation(StreamTokenizer tokenizer, int line) throws IOException {
        int next = tokenizer.nextToken();
        if (next == StreamTokenizer.TT_EOL) {
            // Valid continuation, do nothing (line will be continued)
        } else {
            // Invalid location for continuation character
            throw new IllegalConfigurationException(String.format(PREMATURE_CONTINUATION_MSG, line + 1));
        }
    }
}
```

---

**How to use:**

```java
RuleSetCreator creator = new RuleSetCreator();
EventLoggerProvider logger = ...; // your implementation
RuleSetParser parser = new RuleSetParser(creator, logger);
try (Reader reader = new FileReader("your-acl-file.acl")) {
    RuleSet ruleSet = parser.parse(reader);
    // use ruleSet
}