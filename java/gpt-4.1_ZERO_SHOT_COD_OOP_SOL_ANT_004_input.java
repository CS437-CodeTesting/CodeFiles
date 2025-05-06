    private void buildNode80()
        throws Exception {
        UaVariableNode node = new PropertyNode(this.context, NodeId.parse("ns=0;i=12664"), new QualifiedName(0, "InputArguments"), new LocalizedText("en", "InputArguments"), LocalizedText.NULL_VALUE, UInteger.valueOf(0L), UInteger.valueOf(0L), new DataValue(Variant.NULL_VALUE), NodeId.parse("ns=0;i=296"), 1, new UInteger[]{}, UByte.valueOf(1), UByte.valueOf(1), 0.0D, false);
        node.addReference(new Reference(NodeId.parse("ns=0;i=12664"), NodeId.parse("ns=0;i=46"), ExpandedNodeId.parse("svr=0;i=12663"), NodeClass.Method, false));
        node.addReference(new Reference(NodeId.parse("ns=0;i=12664"), NodeId.parse("ns=0;i=40"), ExpandedNodeId.parse("svr=0;i=68"), NodeClass.VariableType, true));
        node.addReference(new Reference(NodeId.parse("ns=0;i=12664"), NodeId.parse("ns=0;i=46"), ExpandedNodeId.parse("svr=0;i=12663"), NodeClass.Method, false));
        String valueXml;
        OpcUaXmlStreamDecoder decoder = new OpcUaXmlStreamDecoder(new StringReader(new String("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:ListOfExtensionObject xmlns=\"http://opcfoundation.org/BinarySchema/\" xmlns:ns2=\"http://opcfoundation.org/UA/2008/02/Types.xsd\" xmlns:ns3=\"http://opcfoundation.org/UA/2011/03/UANodeSet.xsd\"><ns2:ExtensionObject><ns2:TypeId><ns2:Identifier>i=297</ns2:Identifier></ns2:TypeId><ns2:Body><ns2:Argument><ns2:Name>Masks</ns2:Name><ns2:DataType><ns2:Identifier>i=7</ns2:Identifier></ns2:DataType><ns2:ValueRank>-1</ns2:ValueRank><ns2:ArrayDimensions/><ns2:Description xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/></ns2:Argument></ns2:Body></ns2:ExtensionObject></ns2:ListOfExtensionObject>")));
        Object valueObject = decoder.readVariantValue();
        DataValue value = new DataValue(new Variant(valueObject));
        node.setValue(value);
        this.nodeManager.addNode(node);
    }