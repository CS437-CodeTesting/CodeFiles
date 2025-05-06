public abstract class BaseObject {

    /**
     * Returns the type identifier of this object.
     * By default, returns the simple class name.
     * Subclasses may override to provide a custom type identifier.
     *
     * @return the type identifier of the object
     */
    public String getType() {
        return getTypeIdentifier();
    }

    /**
     * Protected method to determine the type identifier.
     * Subclasses can override this to customize type identification.
     *
     * @return the type identifier
     */
    protected String getTypeIdentifier() {
        return this.getClass().getSimpleName();
    }

    // Prevent instantiation of data-only objects by not exposing any setters or public fields.
    // Additional meaningful behavior can be added here as needed by subclasses.
}