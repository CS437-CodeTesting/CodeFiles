import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

// Assume Bytes and Util classes are available as in the original code.

public final class PhTypeUtil {

    private PhTypeUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // Codec registry
    private static final Map<PhType, PhTypeCodec<?>> CODECS = new EnumMap<>(PhType.class);

    static {
        // Register codecs for each type
        CODECS.put(PhType.INTEGER, new IntegerCodec());
        CODECS.put(PhType.UNSIGNED_INT, new UnsignedIntCodec());
        CODECS.put(PhType.BIGINT, new LongCodec());
        CODECS.put(PhType.UNSIGNED_LONG, new UnsignedLongCodec());
        CODECS.put(PhType.SMALLINT, new ShortCodec());
        CODECS.put(PhType.UNSIGNED_SMALLINT, new UnsignedShortCodec());
        CODECS.put(PhType.TINYINT, new ByteCodec());
        CODECS.put(PhType.UNSIGNED_TINYINT, new UnsignedByteCodec());
        CODECS.put(PhType.FLOAT, new FloatCodec());
        CODECS.put(PhType.UNSIGNED_FLOAT, new UnsignedFloatCodec());
        CODECS.put(PhType.DOUBLE, new DoubleCodec());
        CODECS.put(PhType.UNSIGNED_DOUBLE, new UnsignedDoubleCodec());
        CODECS.put(PhType.BOOLEAN, new BooleanCodec());
        CODECS.put(PhType.DATE, new DateCodec());
        CODECS.put(PhType.TIME, new TimeCodec());
        CODECS.put(PhType.TIMESTAMP, new TimestampCodec());
        CODECS.put(PhType.UNSIGNED_DATE, new UnsignedDateCodec());
        CODECS.put(PhType.UNSIGNED_TIME, new UnsignedTimeCodec());
        CODECS.put(PhType.UNSIGNED_TIMESTAMP, new UnsignedTimestampCodec());
        CODECS.put(PhType.VARBINARY, new VarBinaryCodec());
        CODECS.put(PhType.VARCHAR, new VarcharCodec());
        CODECS.put(PhType.DECIMAL, new DecimalCodec());
        // DEFAULT handled specially
    }

    public static byte[] toBytes(Object v, PhType phType) {
        if (v == null) return null;
        if (phType == PhType.DEFAULT) {
            PhType actualType = PhType.getType(v.getClass());
            if (actualType != null && actualType != PhType.DEFAULT) {
                return toBytes(v, actualType);
            }
            throw new IllegalArgumentException("Cannot determine PhType for value: " + v.getClass());
        }
        PhTypeCodec<?> codec = CODECS.get(phType);
        if (codec == null) {
            throw new UnsupportedOperationException("No codec registered for PhType: " + phType);
        }
        return codec.encode(v);
    }

    public static Object toObject(byte[] b, PhType phType) {
        if (b == null) return null;
        if (phType == PhType.DEFAULT) {
            // Default to VARCHAR
            return CODECS.get(PhType.VARCHAR).decode(b);
        }
        PhTypeCodec<?> codec = CODECS.get(phType);
        if (codec == null) {
            throw new UnsupportedOperationException("No codec registered for PhType: " + phType);
        }
        return codec.decode(b);
    }

    // --- Codec Interface and Implementations ---

    private interface PhTypeCodec<T> {
        byte[] encode(Object value);
        T decode(byte[] bytes);
    }

    private static class IntegerCodec implements PhTypeCodec<Integer> {
        @Override
        public byte[] encode(Object value) {
            int v = ((Number) value).intValue();
            byte[] b = new byte[Bytes.SIZEOF_INT];
            encodeInt(v, b, 0);
            return b;
        }
        @Override
        public Integer decode(byte[] bytes) {
            return decodeInt(bytes, 0);
        }
    }

    private static class UnsignedIntCodec implements PhTypeCodec<Integer> {
        @Override
        public byte[] encode(Object value) {
            int v = ((Number) value).intValue();
            if (v < 0) throw new IllegalArgumentException("Unsigned int cannot be negative: " + v);
            byte[] b = new byte[Bytes.SIZEOF_INT];
            Bytes.putInt(b, 0, v);
            return b;
        }
        @Override
        public Integer decode(byte[] bytes) {
            int v = Bytes.toInt(bytes, 0);
            if (v < 0) throw new IllegalArgumentException("Decoded unsigned int is negative: " + v);
            return v;
        }
    }

    private static class LongCodec implements PhTypeCodec<Long> {
        @Override
        public byte[] encode(Object value) {
            long v = ((Number) value).longValue();
            byte[] b = new byte[Bytes.SIZEOF_LONG];
            encodeLong(v, b, 0);
            return b;
        }
        @Override
        public Long decode(byte[] bytes) {
            return decodeLong(bytes, 0);
        }
    }

    private static class UnsignedLongCodec implements PhTypeCodec<Long> {
        @Override
        public byte[] encode(Object value) {
            long v = ((Number) value).longValue();
            if (v < 0) throw new IllegalArgumentException("Unsigned long cannot be negative: " + v);
            byte[] b = new byte[Bytes.SIZEOF_LONG];
            Bytes.putLong(b, 0, v);
            return b;
        }
        @Override
        public Long decode(byte[] bytes) {
            long v = 0;
            for (int i = 0; i < Bytes.SIZEOF_LONG; i++) {
                v <<= 8;
                v |= bytes[i] & 0xFF;
            }
            if (v < 0) throw new IllegalArgumentException("Decoded unsigned long is negative: " + v);
            return v;
        }
    }

    private static class ShortCodec implements PhTypeCodec<Short> {
        @Override
        public byte[] encode(Object value) {
            short v = ((Number) value).shortValue();
            byte[] b = new byte[Bytes.SIZEOF_SHORT];
            encodeShort(v, b, 0);
            return b;
        }
        @Override
        public Short decode(byte[] bytes) {
            return decodeShort(bytes, 0);
        }
    }

    private static class UnsignedShortCodec implements PhTypeCodec<Short> {
        @Override
        public byte[] encode(Object value) {
            short v = ((Number) value).shortValue();
            if (v < 0) throw new IllegalArgumentException("Unsigned short cannot be negative: " + v);
            byte[] b = new byte[Bytes.SIZEOF_SHORT];
            Bytes.putShort(b, 0, v);
            return b;
        }
        @Override
        public Short decode(byte[] bytes) {
            short v = Bytes.toShort(bytes, 0);
            if (v < 0) throw new IllegalArgumentException("Decoded unsigned short is negative: " + v);
            return v;
        }
    }

    private static class ByteCodec implements PhTypeCodec<Byte> {
        @Override
        public byte[] encode(Object value) {
            byte v = ((Number) value).byteValue();
            byte[] b = new byte[Bytes.SIZEOF_BYTE];
            b[0] = (byte) (v ^ 0x80);
            return b;
        }
        @Override
        public Byte decode(byte[] bytes) {
            return (byte) (bytes[0] ^ 0x80);
        }
    }

    private static class UnsignedByteCodec implements PhTypeCodec<Byte> {
        @Override
        public byte[] encode(Object value) {
            byte v = ((Number) value).byteValue();
            if (v < 0) throw new IllegalArgumentException("Unsigned byte cannot be negative: " + v);
            return new byte[] { v };
        }
        @Override
        public Byte decode(byte[] bytes) {
            byte v = bytes[0];
            if (v < 0) throw new IllegalArgumentException("Decoded unsigned byte is negative