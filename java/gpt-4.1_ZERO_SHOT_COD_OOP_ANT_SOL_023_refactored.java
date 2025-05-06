import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.TException;
import java.util.BitSet;
import java.util.Objects;

/**
 * Handles serialization and deserialization of updateWorkflow_result objects using TupleScheme.
 * Encapsulates all related logic, validation, and error handling.
 */
public final class UpdateWorkflowResultTupleScheme extends org.apache.thrift.scheme.TupleScheme<updateWorkflow_result> {

    /**
     * Serializes the updateWorkflow_result struct to the given protocol.
     * @param prot The protocol to write to.
     * @param struct The updateWorkflow_result instance.
     * @throws TException If serialization fails.
     */
    @Override
    public void write(TProtocol prot, updateWorkflow_result struct) throws TException {
        Objects.requireNonNull(prot, "Protocol cannot be null");
        Objects.requireNonNull(struct, "updateWorkflow_result cannot be null");

        if (!(prot instanceof TTupleProtocol)) {
            throw new IllegalArgumentException("Protocol must be TTupleProtocol");
        }

        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet(1);

        if (struct.isSetRse()) {
            optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);

        if (struct.isSetRse()) {
            validateRse(struct.rse);
            struct.rse.write(oprot);
        }
    }

    /**
     * Deserializes the updateWorkflow_result struct from the given protocol.
     * @param prot The protocol to read from.
     * @param struct The updateWorkflow_result instance to populate.
     * @throws TException If deserialization fails.
     */
    @Override
    public void read(TProtocol prot, updateWorkflow_result struct) throws TException {
        Objects.requireNonNull(prot, "Protocol cannot be null");
        Objects.requireNonNull(struct, "updateWorkflow_result cannot be null");

        if (!(prot instanceof TTupleProtocol)) {
            throw new IllegalArgumentException("Protocol must be TTupleProtocol");
        }

        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);

        if (incoming.get(0)) {
            struct.rse = createRegistryServiceException();
            struct.rse.read(iprot);
            struct.setRseIsSet(true);
            validateRse(struct.rse);
        } else {
            struct.rse = null;
            struct.setRseIsSet(false);
        }
    }

    /**
     * Validates the RegistryServiceException instance.
     * Throws TException if validation fails.
     */
    private void validateRse(org.apache.airavata.registry.api.exception.RegistryServiceException rse) throws TException {
        if (rse == null) {
            throw new TException("RegistryServiceException cannot be null when set.");
        }
        // Add further validation logic as needed
    }

    /**
     * Factory method for RegistryServiceException.
     * Can be extended for dependency injection or mocking.
     */
    protected org.apache.airavata.registry.api.exception.RegistryServiceException createRegistryServiceException() {
        return new org.apache.airavata.registry.api.exception.RegistryServiceException();
    }
}