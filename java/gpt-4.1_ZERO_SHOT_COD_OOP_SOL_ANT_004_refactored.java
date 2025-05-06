import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NodeBuilder {

    // Constants for NodeIds, QualifiedNames, etc.
    private static final String INPUT_ARGUMENTS_NODE_ID = "ns=0;i=12664";
    private static final String INPUT_ARGUMENTS_BROWSE_NAME = "InputArguments";
    private static final int NAMESPACE_INDEX = 0;
    private static final String LOCALE_EN = "en";
    private static final String INPUT_ARGUMENTS_DISPLAY_NAME = "InputArguments";
    private static final long ZERO_UINT = 0L;
    private static final String DATA_TYPE_NODE_ID = "ns=0;i=296";
    private static final int VALUE_RANK = 1;
    private static final UInteger[] EMPTY_UINT_ARRAY = new UInteger[]{};
    private static final short ACCESS_LEVEL = 1;
    private static final double MINIMUM_SAMPLING_INTERVAL = 0.0D;
    private static final boolean HISTORIZING = false;

    // Reference constants
    private static final String HAS_PROPERTY_REF_TYPE_ID = "ns=0;i=46";
    private static final String HAS_TYPE_DEFINITION_REF_TYPE_ID = "ns=0;i=40";
    private static final String PARENT_METHOD_EXPANDED_NODE_ID = "svr=0;i=12663";
    private static final String ARGUMENTS_VARIABLE_TYPE_EXPANDED_NODE_ID = "svr=0;i=68";
    private static final NodeClass PARENT_METHOD_NODE_CLASS = NodeClass.Method;
    private static final NodeClass ARGUMENTS_VARIABLE_TYPE_NODE_CLASS = NodeClass.VariableType;

    // XML for InputArguments value
    private static final String INPUT_ARGUMENTS_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<ns2:ListOfExtensionObject xmlns=\"http://opcfoundation.org/BinarySchema/\" " +
            "xmlns:ns2=\"http://opcfoundation.org/UA/2008/02/Types.xsd\" " +
            "xmlns:ns3=\"http://opcfoundation.org/UA/2011/03/UANodeSet.xsd\">" +
            "<ns2:ExtensionObject>" +
            "<ns2:TypeId><ns2:Identifier>i=297</ns2:Identifier></ns2:TypeId>" +
            "<ns2:Body><ns2:Argument>" +
            "<ns2:Name>Masks</ns2:Name>" +
            "<ns2:DataType><ns2:Identifier>i=7</ns2:Identifier></ns2:DataType>" +
            "<ns2:ValueRank>-1</ns2:ValueRank>" +
            "<ns2:ArrayDimensions/>" +
            "<ns2:Description xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
            "</ns2:Argument></ns2:Body></ns2:ExtensionObject></ns2:ListOfExtensionObject>";

    private final NodeManager nodeManager;
    private final Context context;

    public NodeBuilder(NodeManager nodeManager, Context context) {
        this.nodeManager = nodeManager;
        this.context = context;
    }

    public void buildNode80() throws Exception {
        UaVariableNode inputArgumentsNode = createInputArgumentsNode();
        addNodeReferences(inputArgumentsNode);
        DataValue inputArgumentsValue = decodeInputArgumentsValue(INPUT_ARGUMENTS_XML);
        inputArgumentsNode.setValue(inputArgumentsValue);
        nodeManager.addNode(inputArgumentsNode);
    }

    private UaVariableNode createInputArgumentsNode() {
        return new PropertyNode(
                context,
                NodeId.parse(INPUT_ARGUMENTS_NODE_ID),
                new QualifiedName(NAMESPACE_INDEX, INPUT_ARGUMENTS_BROWSE_NAME),
                new LocalizedText(LOCALE_EN, INPUT_ARGUMENTS_DISPLAY_NAME),
                LocalizedText.NULL_VALUE,
                UInteger.valueOf(ZERO_UINT),
                UInteger.valueOf(ZERO_UINT),
                new DataValue(Variant.NULL_VALUE),
                NodeId.parse(DATA_TYPE_NODE_ID),
                VALUE_RANK,
                EMPTY_UINT_ARRAY,
                UByte.valueOf(ACCESS_LEVEL),
                UByte.valueOf(ACCESS_LEVEL),
                MINIMUM_SAMPLING_INTERVAL,
                HISTORIZING
        );
    }

    private void addNodeReferences(UaVariableNode node) {
        // Reference to parent method (HasProperty)
        node.addReference(new Reference(
                NodeId.parse(INPUT_ARGUMENTS_NODE_ID),
                NodeId.parse(HAS_PROPERTY_REF_TYPE_ID),
                ExpandedNodeId.parse(PARENT_METHOD_EXPANDED_NODE_ID),
                PARENT_METHOD_NODE_CLASS,
                false
        ));
        // Reference to Arguments VariableType (HasTypeDefinition)
        node.addReference(new Reference(
                NodeId.parse(INPUT_ARGUMENTS_NODE_ID),
                NodeId.parse(HAS_TYPE_DEFINITION_REF_TYPE_ID),
                ExpandedNodeId.parse(ARGUMENTS_VARIABLE_TYPE_EXPANDED_NODE_ID),
                ARGUMENTS_VARIABLE_TYPE_NODE_CLASS,
                true
        ));
        // (Optional) Duplicate reference as in original code, if required
        node.addReference(new Reference(
                NodeId.parse(INPUT_ARGUMENTS_NODE_ID),
                NodeId.parse(HAS_PROPERTY_REF_TYPE_ID),
                ExpandedNodeId.parse(PARENT_METHOD_EXPANDED_NODE_ID),
                PARENT_METHOD_NODE_CLASS,
                false
        ));
    }

    private DataValue decodeInputArgumentsValue(String xml) throws Exception {
        try (OpcUaXmlStreamDecoder decoder = new OpcUaXmlStreamDecoder(new StringReader(xml))) {
            Object valueObject = decoder.readVariantValue();
            return new DataValue(new Variant(valueObject));
        }
    }
}