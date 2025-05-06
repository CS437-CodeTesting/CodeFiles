public class LdapComparatorFactory {

    private final SchemaManager schemaManager;
    private final CustomClassLoader classLoader;
    private final Logger log;
    private final I18n i18n;

    public LdapComparatorFactory(SchemaManager schemaManager, CustomClassLoader classLoader, Logger log, I18n i18n) {
        this.schemaManager = schemaManager;
        this.classLoader = classLoader;
        this.log = log;
        this.i18n = i18n;
    }

    public LdapComparator<?> createComparator(String oid, String className, Attribute byteCode) throws LdapException {
        Class<?> clazz = loadComparatorClass(className, byteCode);
        String byteCodeStr = (byteCode != null) ? encodeByteCode(byteCode) : StringConstants.EMPTY;
        LdapComparator<?> comparator = instantiateComparator(clazz, oid, className);
        validateComparatorOid(comparator, oid);
        initializeComparator(comparator, className, byteCodeStr);
        return comparator;
    }

    private Class<?> loadComparatorClass(String className, Attribute byteCode) throws LdapException {
        try {
            if (byteCode == null) {
                return Class.forName(className);
            } else {
                classLoader.setAttribute(byteCode);
                return classLoader.loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            String errorMsg = (byteCode == null)
                ? i18n.err(I18n.ERR_16056_CANNOT_FIND_CMP_CTOR, className)
                : i18n.err(I18n.ERR_16058_CANNOT_LOAD_CMP_CTOR, className);
            log.error(errorMsg);
            throw new LdapSchemaException(
                (byteCode == null)
                    ? i18n.err(I18n.ERR_16057_CANNOT_FIND_CMP_CLASS, e.getMessage())
                    : i18n.err(I18n.ERR_16059_CANNOT_LOAD_CMP_CLASS, e.getMessage())
            );
        }
    }

    private String encodeByteCode(Attribute byteCode) {
        return new String(Base64.encode(byteCode.getBytes()));
    }

    private LdapComparator<?> instantiateComparator(Class<?> clazz, String oid, String className) throws LdapException {
        // Try constructor with String argument (OID)
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(String.class);
            return (LdapComparator<?>) ctor.newInstance(oid);
        } catch (NoSuchMethodException e) {
            // Try no-arg constructor
            return instantiateNoArgComparator(clazz, oid, className, e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            handleInstantiationException(e, className, true);
            return null; // Unreachable, but required by compiler
        }
    }

    private LdapComparator<?> instantiateNoArgComparator(Class<?> clazz, String oid, String className, NoSuchMethodException cause) throws LdapException {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            LdapComparator<?> comparator = (LdapComparator<?>) ctor.newInstance();
            return comparator;
        } catch (NoSuchMethodException e) {
            log.error(i18n.err(I18n.ERR_16066_CANNOT_FIND_CMP_CTOR_METH_CLASS, className));
            throw new LdapSchemaException(i18n.err(I18n.ERR_16067_CANNOT_FIND_CMP_CTOR_METH, e.getMessage()));
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            handleInstantiationException(e, className, false);
            return null; // Unreachable, but required by compiler
        }
    }

    private void handleInstantiationException(Exception e, String className, boolean withOid) throws LdapSchemaException {
        String errorMsg;
        if (e instanceof InvocationTargetException) {
            errorMsg = withOid
                ? i18n.err(I18n.ERR_16060_CANNOT_INVOKE_CMP_CTOR, className)
                : i18n.err(I18n.ERR_16060_CANNOT_INVOKE_CMP_CTOR, className);
        } else if (e instanceof InstantiationException) {
            errorMsg = i18n.err(I18n.ERR_16062_CANNOT_INST_CMP_CTOR_CLASS, className);
        } else if (e instanceof IllegalAccessException) {
            errorMsg = i18n.err(I18n.ERR_16064_CANNOT_ACCESS_CMP_CTOR, className);
        } else {
            errorMsg = i18n.err(I18n.ERR_16063_CANNOT_INST_CMP_CLASS, e.getMessage());
        }
        log.error(errorMsg);
        throw new LdapSchemaException(i18n.err(I18n.ERR_16063_CANNOT_INST_CMP_CLASS, e.getMessage()));
    }

    private void validateComparatorOid(LdapComparator<?> comparator, String expectedOid) throws LdapInvalidAttributeValueException {
        if (comparator.getOid() != null && !comparator.getOid().equals(expectedOid)) {
            String msg = i18n.err(I18n.ERR_16021_DIFFERENT_COMPARATOR_OID, expectedOid, comparator.getOid());
            throw new LdapInvalidAttributeValueException(ResultCodeEnum.UNWILLING_TO_PERFORM, msg);
        }
    }

    private void initializeComparator(LdapComparator<?> comparator, String className, String byteCodeStr) {
        comparator.setBytecode(byteCodeStr);
        comparator.setFqcn(className);
        comparator.setSchemaManager(schemaManager);
    }
}