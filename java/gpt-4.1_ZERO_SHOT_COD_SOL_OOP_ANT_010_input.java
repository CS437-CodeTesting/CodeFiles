      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, getNamespaceIteratorSetting_result struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
        java.util.BitSet incoming = iprot.readBitSet(4);
        if (incoming.get(0)) {
          struct.success = new IteratorSetting();
          struct.success.read(iprot);
          struct.setSuccessIsSet(true);
        }
        if (incoming.get(1)) {
          struct.ouch1 = new AccumuloException();
          struct.ouch1.read(iprot);
          struct.setOuch1IsSet(true);
        }
        if (incoming.get(2)) {
          struct.ouch2 = new AccumuloSecurityException();
          struct.ouch2.read(iprot);
          struct.setOuch2IsSet(true);
        }
        if (incoming.get(3)) {
          struct.ouch3 = new NamespaceNotFoundException();
          struct.ouch3.read(iprot);
          struct.setOuch3IsSet(true);
        }
      }