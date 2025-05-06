// Interface for sequencing different semantic objects
public interface SemanticSequencer<T extends EObject> {
    void sequence(ISerializationContext context, T semanticObject);
    boolean canHandle(EObject semanticObject);
}

// Handler for Child1
public class Child1Sequencer implements SemanticSequencer<Child1> {
    @Override
    public void sequence(ISerializationContext context, Child1 semanticObject) {
        // Original sequence_Child1 logic here
    }

    @Override
    public boolean canHandle(EObject semanticObject) {
        return semanticObject instanceof Child1;
    }
}

// Handler for Child2
public class Child2Sequencer implements SemanticSequencer<Child2> {
    @Override
    public void sequence(ISerializationContext context, Child2 semanticObject) {
        // Original sequence_Child2 logic here
    }

    @Override
    public boolean canHandle(EObject semanticObject) {
        return semanticObject instanceof Child2;
    }
}

// Handler for Model
public class ModelSequencer implements SemanticSequencer<Model> {
    @Override
    public void sequence(ISerializationContext context, Model semanticObject) {
        // Original sequence_Model logic here
    }

    @Override
    public boolean canHandle(EObject semanticObject) {
        return semanticObject instanceof Model;
    }
}

// Main sequencer class
public class Bug250313SemanticSequencer {

    private final List<SemanticSequencer<? extends EObject>> sequencers;
    private final DiagnosticProvider diagnosticProvider;
    private final ErrorAcceptor errorAcceptor;

    public Bug250313SemanticSequencer(DiagnosticProvider diagnosticProvider, ErrorAcceptor errorAcceptor) {
        this.diagnosticProvider = diagnosticProvider;
        this.errorAcceptor = errorAcceptor;
        this.sequencers = Arrays.asList(
            new Child1Sequencer(),
            new Child2Sequencer(),
            new ModelSequencer()
        );
    }

    public void sequence(ISerializationContext context, EObject semanticObject) {
        EPackage epackage = semanticObject.eClass().getEPackage();
        if (epackage == Bug250313Package.eINSTANCE) {
            for (SemanticSequencer<? extends EObject> sequencer : sequencers) {
                if (sequencer.canHandle(semanticObject)) {
                    // Safe cast due to canHandle check
                    @SuppressWarnings("unchecked")
                    SemanticSequencer<EObject> handler = (SemanticSequencer<EObject>) sequencer;
                    handler.sequence(context, semanticObject);
                    return;
                }
            }
        }
        if (errorAcceptor != null) {
            errorAcceptor.accept(
                diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context)
            );
        }
    }
}