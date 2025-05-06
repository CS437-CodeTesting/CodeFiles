public class JvmConstructorTransformer {

    private final TypesFactory typesFactory;
    private final Associator associator;
    private final JvmTypesBuilder jvmTypesBuilder;

    public JvmConstructorTransformer(TypesFactory typesFactory, Associator associator, JvmTypesBuilder jvmTypesBuilder) {
        this.typesFactory = typesFactory;
        this.associator = associator;
        this.jvmTypesBuilder = jvmTypesBuilder;
    }

    public void transform(XtendConstructor source, JvmGenericType container) {
        JvmConstructorBuilder builder = new JvmConstructorBuilder(typesFactory, associator, jvmTypesBuilder);
        JvmConstructor constructor = builder.build(source, container);
        container.getMembers().add(constructor);
    }

    private static class JvmConstructorBuilder {
        private final TypesFactory typesFactory;
        private final Associator associator;
        private final JvmTypesBuilder jvmTypesBuilder;

        public JvmConstructorBuilder(TypesFactory typesFactory, Associator associator, JvmTypesBuilder jvmTypesBuilder) {
            this.typesFactory = typesFactory;
            this.associator = associator;
            this.jvmTypesBuilder = jvmTypesBuilder;
        }

        public JvmConstructor build(XtendConstructor source, JvmGenericType container) {
            JvmConstructor constructor = createConstructor(container);
            associateSource(source, constructor);
            setVisibilityAndName(source, container, constructor);
            addParameters(source, constructor);
            addTypeParameters(source, constructor);
            addExceptions(source, constructor);
            addAnnotations(source, constructor);
            setBody(source, constructor);
            copyDocumentation(source, constructor);
            return constructor;
        }

        private JvmConstructor createConstructor(JvmGenericType container) {
            JvmConstructor constructor = typesFactory.createJvmConstructor();
            constructor.setSimpleName(container.getSimpleName());
            return constructor;
        }

        private void associateSource(XtendConstructor source, JvmConstructor constructor) {
            associator.associatePrimary(source, constructor);
        }

        private void setVisibilityAndName(XtendConstructor source, JvmGenericType container, JvmConstructor constructor) {
            constructor.setVisibility(source.getVisibility());
            // Name already set in createConstructor
        }

        private void addParameters(XtendConstructor source, JvmConstructor constructor) {
            for (XtendParameter parameter : source.getParameters()) {
                translateParameter(constructor, parameter);
            }
        }

        private void addTypeParameters(XtendConstructor source, JvmConstructor constructor) {
            copyAndFixTypeParameters(source.getTypeParameters(), constructor);
        }

        private void addExceptions(XtendConstructor source, JvmConstructor constructor) {
            for (JvmTypeReference exception : source.getExceptions()) {
                constructor.getExceptions().add(jvmTypesBuilder.cloneWithProxies(exception));
            }
        }

        private void addAnnotations(XtendConstructor source, JvmConstructor constructor) {
            translateAnnotationsTo(source.getAnnotations(), constructor);
        }

        private void setBody(XtendConstructor source, JvmConstructor constructor) {
            setBody(constructor, source.getExpression());
        }

        private void copyDocumentation(XtendConstructor source, JvmConstructor constructor) {
            jvmTypesBuilder.copyDocumentationTo(source, constructor);
        }

        // These methods are assumed to be provided elsewhere or can be injected as dependencies.
        private void translateParameter(JvmConstructor constructor, XtendParameter parameter) {
            // Implementation provided elsewhere
        }

        private void copyAndFixTypeParameters(List<XtendTypeParameter> typeParameters, JvmConstructor constructor) {
            // Implementation provided elsewhere
        }

        private void translateAnnotationsTo(List<XAnnotation> annotations, JvmConstructor constructor) {
            // Implementation provided elsewhere
        }

        private void setBody(JvmConstructor constructor, XExpression expression) {
            // Implementation provided elsewhere
        }
    }
}