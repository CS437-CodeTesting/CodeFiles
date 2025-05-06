import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Decodes debug information from a DEX file for a given method.
 * This class is responsible for parsing the debug info byte array and producing
 * structured position and local variable information.
 */
public final class DebugInfoDecoder {

    // --- Constants for debug opcodes ---
    private static final int DBG_END_SEQUENCE = 0x00;
    private static final int DBG_ADVANCE_PC = 0x01;
    private static final int DBG_ADVANCE_LINE = 0x02;
    private static final int DBG_START_LOCAL = 0x03;
    private static final int DBG_START_LOCAL_EXTENDED = 0x04;
    private static final int DBG_END_LOCAL = 0x05;
    private static final int DBG_RESTART_LOCAL = 0x06;
    private static final int DBG_SET_PROLOGUE_END = 0x07;
    private static final int DBG_SET_EPILOGUE_BEGIN = 0x08;
    private static final int DBG_SET_FILE = 0x09;
    private static final int DBG_FIRST_SPECIAL = 0x0A;
    private static final int DBG_LINE_BASE = -4;
    private static final int DBG_LINE_RANGE = 15;

    // --- Immutable fields ---
    private final byte[] encoded;
    private final int codeSize;
    private final int regSize;
    private final boolean isStatic;
    private final Prototype prototype;
    private final DexFile dexFile;
    private final int thisStringIdx;

    // --- Decoded results ---
    private final List<PositionEntry> positions = new ArrayList<>();
    private final List<LocalEntry> locals = new ArrayList<>();

    // --- Decoding state ---
    private int line = 1;
    private int address = 0;
    private final LocalEntry[] lastEntryForReg;

    /**
     * Constructs a DebugInfoDecoder.
     */
    public DebugInfoDecoder(byte[] encoded, int codeSize, int regSize,
                            boolean isStatic, CstMethodRef methodRef, DexFile dexFile) {
        this.encoded = Objects.requireNonNull(encoded, "encoded == null");
        this.codeSize = codeSize;
        this.regSize = regSize;
        this.isStatic = isStatic;
        this.prototype = Objects.requireNonNull(methodRef, "methodRef == null").getPrototype();
        this.dexFile = Objects.requireNonNull(dexFile, "dexFile == null");
        this.lastEntryForReg = new LocalEntry[regSize];
        this.thisStringIdx = findThisStringIndex(dexFile);
    }

    /**
     * Returns the decoded position entries.
     */
    public List<PositionEntry> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    /**
     * Returns the decoded local variable entries.
     */
    public List<LocalEntry> getLocals() {
        return Collections.unmodifiableList(locals);
    }

    /**
     * Decodes the debug info byte array.
     */
    public void decode() {
        try {
            decodeInternal();
        } catch (Exception ex) {
            throw new DebugInfoDecodeException("Error decoding debug info", ex);
        }
    }

    // --- Internal decoding logic ---

    private void decodeInternal() throws IOException {
        ByteInput input = new ByteArrayByteInput(encoded);

        line = Leb128.readUnsignedLeb128(input);
        int paramCount = Leb128.readUnsignedLeb128(input);
        StdTypeList paramTypes = prototype.getParameterTypes();
        int curReg = getParamBase();

        if (paramCount != paramTypes.size()) {
            throw new DebugInfoDecodeException("Mismatch between parameters_size and prototype");
        }

        if (!isStatic) {
            // Add implicit 'this' parameter
            LocalEntry thisEntry = LocalEntry.createStart(0, curReg, thisStringIdx, 0, 0);
            addLocalEntry(thisEntry);
            curReg++;
        }

        for (int i = 0; i < paramCount; i++) {
            Type paramType = paramTypes.getType(i);
            int nameIdx = readStringIndex(input);
            LocalEntry entry = LocalEntry.createStart(0, curReg, nameIdx, 0, 0);
            addLocalEntry(entry);
            curReg += paramType.getCategory();
        }

        while (true) {
            int opcode = input.readByte() & 0xFF;
            if (opcode == DBG_END_SEQUENCE) {
                break;
            }
            handleOpcode(opcode, input);
        }
    }

    private void handleOpcode(int opcode, ByteInput input) throws IOException {
        switch (opcode) {
            case DBG_START_LOCAL:
                handleStartLocal(input, false);
                break;
            case DBG_START_LOCAL_EXTENDED:
                handleStartLocal(input, true);
                break;
            case DBG_RESTART_LOCAL:
                handleRestartLocal(input);
                break;
            case DBG_END_LOCAL:
                handleEndLocal(input);
                break;
            case DBG_ADVANCE_PC:
                address += Leb128.readUnsignedLeb128(input);
                break;
            case DBG_ADVANCE_LINE:
                line += Leb128.readSignedLeb128(input);
                break;
            case DBG_SET_PROLOGUE_END:
            case DBG_SET_EPILOGUE_BEGIN:
            case DBG_SET_FILE:
                // No-op for now; could be extended for richer debug info.
                break;
            default:
                if (opcode < DBG_FIRST_SPECIAL) {
                    throw new DebugInfoDecodeException("Invalid extended opcode: " + opcode);
                }
                int adjOpcode = opcode - DBG_FIRST_SPECIAL;
                address += adjOpcode / DBG_LINE_RANGE;
                line += DBG_LINE_BASE + (adjOpcode % DBG_LINE_RANGE);
                positions.add(new PositionEntry(address, line));
                break;
        }
    }

    private void handleStartLocal(ByteInput input, boolean extended) throws IOException {
        int reg = Leb128.readUnsignedLeb128(input);
        int nameIdx = readStringIndex(input);
        int typeIdx = readStringIndex(input);
        int sigIdx = extended ? readStringIndex(input) : 0;
        LocalEntry entry = LocalEntry.createStart(address, reg, nameIdx, typeIdx, sigIdx);
        addLocalEntry(entry);
    }

    private void handleRestartLocal(ByteInput input) throws IOException {
        int reg = Leb128.readUnsignedLeb128(input);
        LocalEntry prev = lastEntryForReg[reg];
        if (prev == null || prev.isStart()) {
            throw new DebugInfoDecodeException("Invalid RESTART_LOCAL for register v" + reg);
        }
        LocalEntry entry = LocalEntry.createStart(address, reg, prev.getNameIndex(), prev.getTypeIndex(), prev.getSignatureIndex());
        addLocalEntry(entry);
    }

    private void handleEndLocal(ByteInput input) throws IOException {
        int reg = Leb128.readUnsignedLeb128(input);
        LocalEntry prev = lastEntryForReg[reg];
        if (prev == null || !prev.isStart()) {
            throw new DebugInfoDecodeException("Invalid END_LOCAL for register v" + reg);
        }
        LocalEntry entry = LocalEntry.createEnd(address, reg, prev.getNameIndex(), prev.getTypeIndex(), prev.getSignatureIndex());
        addLocalEntry(entry);
    }

    private void addLocalEntry(LocalEntry entry) {
        locals.add(entry);
        lastEntryForReg[entry.getRegister()] = entry;
    }

    private int readStringIndex(ByteInput input) throws IOException {
        int offsetIndex = Leb128.readUnsignedLeb128(input);
        return offsetIndex - 1;
    }

    private int getParamBase() {
        return regSize - prototype.getParameterTypes().getWordCount() - (isStatic ? 0 : 1);
    }

    private int findThis