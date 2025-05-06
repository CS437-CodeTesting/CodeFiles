  @Implementation
  protected synchronized int checkSlicePermission(Uri uri, int pid, int uid) {
    if (uid == 0) {
      return PackageManager.PERMISSION_GRANTED;
    }
    Collection<Uri> uris = packageUidsToPermissionGrantedSliceUris.get(uid);
    if (uris != null && uris.contains(uri)) {
      return PackageManager.PERMISSION_GRANTED;
    }
    return PackageManager.PERMISSION_DENIED;
  }