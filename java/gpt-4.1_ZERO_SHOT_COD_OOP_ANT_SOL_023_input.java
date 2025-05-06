    private static class updateWorkflow_resultTupleScheme extends TupleScheme<updateWorkflow_result> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, updateWorkflow_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetRse()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetRse()) {
          struct.rse.write(oprot);
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, updateWorkflow_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          struct.rse = new org.apache.airavata.registry.api.exception.RegistryServiceException();
          struct.rse.read(iprot);
          struct.setRseIsSet(true);
        }
      }
    }