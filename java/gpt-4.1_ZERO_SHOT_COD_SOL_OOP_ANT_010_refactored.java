@Override
public void read(org.apache.thrift.protocol.TProtocol prot, getNamespaceIteratorSetting_result struct) throws org.apache.thrift.TException {
    org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
    java.util.BitSet incoming = iprot.readBitSet(4);

    if (incoming.get(0)) {
        struct.setSuccess(readIteratorSetting(iprot));
    }
    if (incoming.get(1)) {
        struct.setOuch1(readAccumuloException(iprot));
    }
    if (incoming.get(2)) {
        struct.setOuch2(readAccumuloSecurityException(iprot));
    }
    if (incoming.get(3)) {
        struct.setOuch3(readNamespaceNotFoundException(iprot));
    }
}

// Helper methods for deserialization, each with a single responsibility

private IteratorSetting readIteratorSetting(org.apache.thrift.protocol.TTupleProtocol iprot) throws org.apache.thrift.TException {
    IteratorSetting setting = new IteratorSetting();
    setting.read(iprot);
    return setting;
}

private AccumuloException readAccumuloException(org.apache.thrift.protocol.TTupleProtocol iprot) throws org.apache.thrift.TException {
    AccumuloException ex = new AccumuloException();
    ex.read(iprot);
    return ex;
}

private AccumuloSecurityException readAccumuloSecurityException(org.apache.thrift.protocol.TTupleProtocol iprot) throws org.apache.thrift.TException {
    AccumuloSecurityException ex = new AccumuloSecurityException();
    ex.read(iprot);
    return ex;
}

private NamespaceNotFoundException readNamespaceNotFoundException(org.apache.thrift.protocol.TTupleProtocol iprot) throws org.apache.thrift.TException {
    NamespaceNotFoundException ex = new NamespaceNotFoundException();
    ex.read(iprot);
    return ex;
}