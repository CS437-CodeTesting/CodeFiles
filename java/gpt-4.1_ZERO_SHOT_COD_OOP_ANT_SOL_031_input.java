@JsonDeserialize(using = _ApplicationInstancesResponse.ApplicationInstancesResponseDeserializer.class)
@Value.Immutable
abstract class _ApplicationInstancesResponse {

    /**
     * The instances
     */
    @AllowNulls
    abstract Map<String, ApplicationInstanceInfo> getInstances();

    static final class ApplicationInstancesResponseDeserializer extends StdDeserializer<ApplicationInstancesResponse> {

        private static final long serialVersionUID = -3557583833091104581L;

        ApplicationInstancesResponseDeserializer() {
            super(ApplicationInstancesResponse.class);
        }

        @Override
        public ApplicationInstancesResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return ApplicationInstancesResponse.builder()
                .instances(p.readValueAs(new TypeReference<Map<String, ApplicationInstanceInfo>>() {

                }))
                .build();
        }
    }

}