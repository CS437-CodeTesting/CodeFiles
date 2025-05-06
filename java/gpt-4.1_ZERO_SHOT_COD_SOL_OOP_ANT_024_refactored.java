import org.graalvm.compiler.core.common.calc.CodeUtil;
import org.graalvm.compiler.core.common.type.*;
import org.graalvm.compiler.graph.NodeClass;
import org.graalvm.compiler.graph.NodeView;
import org.graalvm.compiler.nodeinfo.NodeInfo;
import org.graalvm.compiler.nodes.*;
import org.graalvm.compiler.nodes.calc.UnaryNode;
import org.graalvm.compiler.nodes.spi.ArithmeticLIRGeneratorTool;
import org.graalvm.compiler.nodes.spi.NodeLIRBuilderTool;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaKind;

/**
 * Node representing a bit scan forward (count trailing zeros) operation.
 */
@NodeInfo(cycles = CYCLES_2, size = SIZE_1)
public final class BitScanForwardNode extends UnaryNode implements ArithmeticLIRLowerable {

    public static final NodeClass<BitScanForwardNode> TYPE = NodeClass.create(BitScanForwardNode.class);

    private static final int INT_MASK = 0xFFFFFFFF;

    /**
     * Constructs a BitScanForwardNode.
     *
     * @param value the value to scan
     * @throws IllegalArgumentException if value is not of kind Int or Long
     */
    public BitScanForwardNode(ValueNode value) {
        super(TYPE, createStamp(value), value);
        JavaKind kind = value.getStackKind();
        if (kind != JavaKind.Int && kind != JavaKind.Long) {
            throw new IllegalArgumentException("BitScanForwardNode only supports Int or Long values, got: " + kind);
        }
    }

    private static Stamp createStamp(ValueNode value) {
        Stamp valueStamp = value.stamp(NodeView.DEFAULT);
        if (!(valueStamp instanceof PrimitiveStamp)) {
            throw new IllegalArgumentException("Value must have a PrimitiveStamp");
        }
        PrimitiveStamp primStamp = (PrimitiveStamp) valueStamp;
        return StampFactory.forInteger(JavaKind.Int, 0, primStamp.getBits());
    }

    @Override
    public Stamp foldStamp(Stamp newStamp) {
        if (!(newStamp instanceof IntegerStamp)) {
            throw new IllegalArgumentException("foldStamp expects IntegerStamp");
        }
        IntegerStamp valueStamp = (IntegerStamp) newStamp;
        int bits = valueStamp.getBits();
        long mask = CodeUtil.mask(bits);

        int firstAlwaysSetBit = BitScanUtils.scan(valueStamp.downMask() & mask);
        int firstMaybeSetBit = BitScanUtils.scan(valueStamp.upMask() & mask);

        int min, max;
        if (firstAlwaysSetBit == BitScanUtils.NOT_FOUND) {
            int lastMaybeSetBit = BitScanReverseNode.scan(valueStamp.upMask() & mask);
            min = firstMaybeSetBit;
            max = lastMaybeSetBit;
        } else {
            min = firstMaybeSetBit;
            max = firstAlwaysSetBit;
        }
        return StampFactory.forInteger(JavaKind.Int, min, max);
    }

    /**
     * Attempts to fold the node if the value is constant.
     *
     * @param value the value node
     * @return a folded ConstantNode if possible, otherwise null
     */
    public static ValueNode tryFold(ValueNode value) {
        if (value.isConstant()) {
            JavaConstant c = value.asJavaConstant();
            if (c.asLong() != 0) {
                int result = value.getStackKind() == JavaKind.Int
                        ? BitScanUtils.scan(c.asInt())
                        : BitScanUtils.scan(c.asLong());
                return ConstantNode.forInt(result);
            }
        }
        return null;
    }

    @Override
    public ValueNode canonical(CanonicalizerTool tool, ValueNode forValue) {
        ValueNode folded = tryFold(forValue);
        return folded != null ? folded : this;
    }

    @Override
    public void generate(NodeLIRBuilderTool builder, ArithmeticLIRGeneratorTool gen) {
        builder.setResult(this, gen.emitBitScanForward(builder.operand(getValue())));
    }

    /**
     * Raw intrinsic for bsf instruction (long).
     *
     * @param v value to scan
     * @return number of trailing zeros or undefined if v == 0
     */
    @NodeIntrinsic
    public static native int unsafeScan(long v);

    /**
     * Raw intrinsic for bsf instruction (int).
     *
     * @param v value to scan
     * @return number of trailing zeros or undefined if v == 0
     */
    @NodeIntrinsic
    public static native int unsafeScan(int v);
}

/**
 * Utility class for bit scan operations.
 */
final class BitScanUtils {

    private BitScanUtils() {
        // Utility class; prevent instantiation.
    }

    public static final int NOT_FOUND = -1;

    /**
     * Returns the number of trailing zeros in the long value, or NOT_FOUND if v == 0.
     *
     * @param v value to scan
     * @return number of trailing zeros, or NOT_FOUND if v == 0
     */
    public static int scan(long v) {
        return v == 0 ? NOT_FOUND : Long.numberOfTrailingZeros(v);
    }

    /**
     * Returns the number of trailing zeros in the int value, or NOT_FOUND if v == 0.
     *
     * @param v value to scan
     * @return number of trailing zeros, or NOT_FOUND if v == 0
     */
    public static int scan(int v) {
        return scan((long) v & 0xFFFFFFFFL);
    }
}