// --- AccountJsonParser.java ---
public interface AccountJsonParser {
    String extractString(JsonElement element, String paramName);
    Long extractLong(JsonElement element, String paramName);
    Integer extractInteger(JsonElement element, String paramName);
    BigDecimal extractBigDecimal(JsonElement element, String paramName, Locale locale);
    LocalDate extractLocalDate(JsonElement element, String paramName);
    boolean extractBoolean(JsonElement element, String paramName);
    JsonElement parse(String json);
    Locale extractLocale(JsonObject jsonObject);
    String extractDateFormat(JsonObject jsonObject);
    // ... other extraction methods as needed
}

// --- DefaultAccountJsonParser.java ---
@Component
public class DefaultAccountJsonParser implements AccountJsonParser {
    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public DefaultAccountJsonParser(FromJsonHelper fromJsonHelper) {
        this.fromJsonHelper = fromJsonHelper;
    }

    @Override
    public String extractString(JsonElement element, String paramName) {
        return fromJsonHelper.extractStringNamed(paramName, element);
    }
    @Override
    public Long extractLong(JsonElement element, String paramName) {
        return fromJsonHelper.extractLongNamed(paramName, element);
    }
    @Override
    public Integer extractInteger(JsonElement element, String paramName) {
        return fromJsonHelper.extractIntegerNamed(paramName, element);
    }
    @Override
    public BigDecimal extractBigDecimal(JsonElement element, String paramName, Locale locale) {
        return fromJsonHelper.extractBigDecimalNamed(paramName, element, locale);
    }
    @Override
    public LocalDate extractLocalDate(JsonElement element, String paramName) {
        return fromJsonHelper.extractLocalDateNamed(paramName, element);
    }
    @Override
    public boolean extractBoolean(JsonElement element, String paramName) {
        return fromJsonHelper.extractBooleanNamed(paramName, element);
    }
    @Override
    public JsonElement parse(String json) {
        return fromJsonHelper.parse(json);
    }
    @Override
    public Locale extractLocale(JsonObject jsonObject) {
        return fromJsonHelper.extractLocaleParameter(jsonObject);
    }
    @Override
    public String extractDateFormat(JsonObject jsonObject) {
        return fromJsonHelper.extractDateFormatParameter(jsonObject);
    }
}

// --- AccountChargeAssembler.java ---
public interface AccountChargeAssembler {
    Set<SavingsAccountCharge> assembleCharges(JsonElement element, String currencyCode);
}

// --- DefaultAccountChargeAssembler.java ---
@Component
public class DefaultAccountChargeAssembler implements AccountChargeAssembler {
    private final SavingsAccountChargeAssembler savingsAccountChargeAssembler;

    @Autowired
    public DefaultAccountChargeAssembler(SavingsAccountChargeAssembler assembler) {
        this.savingsAccountChargeAssembler = assembler;
    }

    @Override
    public Set<SavingsAccountCharge> assembleCharges(JsonElement element, String currencyCode) {
        return savingsAccountChargeAssembler.fromParsedJson(element, currencyCode);
    }
}

// --- AccountValidationService.java ---
public interface AccountValidationService {
    void validateClientIsActive(Client client, Long clientId);
    void validateGroupIsActive(Group group, Long groupId);
    void validateClientInGroup(Client client, Group group, Long clientId, Long groupId);
    void validateWithHoldTax(boolean withHoldTax, SavingsProduct product);
    // ... other validation methods as needed
}

// --- DefaultAccountValidationService.java ---
@Component
public class DefaultAccountValidationService implements AccountValidationService {
    @Override
    public void validateClientIsActive(Client client, Long clientId) {
        if (client.isNotActive()) {
            throw new ClientNotActiveException(clientId);
        }
    }
    @Override
    public void validateGroupIsActive(Group group, Long groupId) {
        if (group.isNotActive()) {
            if (group.isCenter()) {
                throw new CenterNotActiveException(groupId);
            }
            throw new GroupNotActiveException(groupId);
        }
    }
    @Override
    public void validateClientInGroup(Client client, Group group, Long clientId, Long groupId) {
        if (!group.hasClientAsMember(client)) {
            throw new ClientNotInGroupException(clientId, groupId);
        }
    }
    @Override
    public void validateWithHoldTax(boolean withHoldTax, SavingsProduct product) {
        if (withHoldTax && product.getTaxGroup() == null) {
            throw new UnsupportedParameterException(Arrays.asList("withHoldTax"));
        }
    }
}

// --- AccountAssembler.java ---
public interface AccountAssembler {
    SavingsAccount assembleAccount(AccountAssemblyRequest request);
    DepositAccountTermAndPreClosure assembleTermAndPreClosure(JsonCommand command, DepositProductTermAndPreClosure productTermAndPreclosure);
    DepositAccountRecurringDetail assembleRecurringDetail(JsonCommand command, DepositRecurringDetail prodRecurringDetail);
}

// --- DefaultAccountAssembler.java ---
@Component
public class DefaultAccountAssembler implements AccountAssembler {
    private final DepositProductAssembler depositProductAssembler;

    @Autowired
    public DefaultAccountAssembler(DepositProductAssembler depositProductAssembler) {
        this.depositProductAssembler = depositProductAssembler;
    }

    @Override
    public SavingsAccount assembleAccount(AccountAssemblyRequest req) {
        // This method contains the logic for assembling SavingsAccount, FixedDepositAccount, or RecurringDepositAccount
        // using the data in AccountAssemblyRequest (which is a DTO containing all needed data).
        // For brevity, the logic is similar to the original, but now all dependencies are injected and
        // all parsing/validation is done outside this method.
        // (Implementation omitted for brevity, but follows the same pattern as the original, using only data from req)
        // ...
        // Return the assembled account
        return req.getAccount();
    }

    @Override
    public DepositAccountTermAndPreClosure assembleTermAndPreClosure(JsonCommand command, DepositProductTermAndPreClosure productTermAndPreclosure) {
        // Similar to original, but only assembling term and preclosure
        // ...
        return DepositAccountTermAndPreClosure.createNew(
            // ... parameters ...
        );
    }

    @Override
    public DepositAccountRecurringDetail assembleRecurringDetail(JsonCommand command, DepositRecurringDetail prodRecurringDetail) {
        // Similar to original, but only assembling recurring detail
        // ...
        return DepositAccountRecurringDetail.createNew(
            // ... parameters ...
        );
    }
}

// --- AccountTransactionAssembler.java ---
public interface AccountTransactionAssembler {
    Collection<SavingsAccountTransactionDTO> assembleBulkTransactions(JsonCommand command, PaymentDetail paymentDetail, AppUser user);
}

// --- DefaultAccountTransactionAssembler.java ---
@Component
public class DefaultAccountTransactionAssembler implements AccountTransactionAssembler {
    private final AccountJsonParser jsonParser;
    private final PaymentDetailAssembler paymentDetailAssembler;

    @Autowired
    public DefaultAccountTransactionAssembler(AccountJsonParser jsonParser, PaymentDetailAssembler paymentDetailAssembler) {
        this.jsonParser = jsonParser;
        this.paymentDetailAssembler = paymentDetailAssembler;
    }

    @Override
    public Collection<SavingsAccountTransactionDTO> assembleBulkTransactions(JsonCommand command, PaymentDetail paymentDetail, AppUser user) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final JsonElement element = jsonParser.parse(json);
        final Collection<SavingsAccountTransactionDTO> transactions = new ArrayList<>();
        final LocalDate transactionDate = jsonParser.extractLocalDate(element, "transactionDate");
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = jsonParser.extractLocale(topLevelJsonElement);
        final String dateFormat = jsonParser.extractDateFormat(topLevelJsonElement);
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withLocale(locale);

        if (element.isJsonObject() && topLevelJsonElement.has("bulkSavingsDueTransactions")
                && topLevelJsonElement.get("bulkSavingsDueTransactions").isJsonArray()) {
            final JsonArray array = topLevelJsonElement.get("bulkSavingsDueTransactions").getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {