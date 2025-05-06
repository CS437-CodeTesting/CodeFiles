public class SlicePermissionChecker {

    // Encapsulated field
    private final Map<Integer, Collection<Uri>> packageUidsToPermissionGrantedSliceUris;

    // Magic number replaced with named constant
    private static final int SYSTEM_UID = 0;

    // Permission result enum for clarity and type safety
    public enum PermissionResult {
        GRANTED,
        DENIED
    }

    public SlicePermissionChecker(Map<Integer, Collection<Uri>> packageUidsToPermissionGrantedSliceUris) {
        this.packageUidsToPermissionGrantedSliceUris = packageUidsToPermissionGrantedSliceUris;
    }

    @Implementation
    protected synchronized PermissionResult checkSlicePermission(Uri uri, int pid, int uid) {
        if (isSystemUid(uid)) {
            return PermissionResult.GRANTED;
        }
        if (isPermissionGrantedForUri(uid, uri)) {
            return PermissionResult.GRANTED;
        }
        return PermissionResult.DENIED;
    }

    // Helper method to encapsulate system UID check
    private boolean isSystemUid(int uid) {
        return uid == SYSTEM_UID;
    }

    // Helper method to encapsulate permission check logic
    private boolean isPermissionGrantedForUri(int uid, Uri uri) {
        Collection<Uri> uris = getPermissionGrantedUrisForUid(uid);
        return uris != null && uris.contains(uri);
    }

    // Encapsulated access to the permission map
    private Collection<Uri> getPermissionGrantedUrisForUid(int uid) {
        return packageUidsToPermissionGrantedSliceUris.get(uid);
    }
}