public class ReflectionModule extends LazyModule {

    private final ReflectionModuleManager modelManager;
    private volatile boolean packagesLoaded = false;

    public ReflectionModule(ReflectionModuleManager reflectionModuleManager) {
        this.modelManager = reflectionModuleManager;
    }

    @Override
    protected AbstractModelLoader getModelLoader() {
        return modelManager.getModelLoader();
    }

    @Override
    public List<Package> getPackages() {
        loadPackagesIfNeeded();
        return super.getPackages();
    }

    /**
     * Ensures that packages are loaded exactly once in a thread-safe manner.
     */
    private void loadPackagesIfNeeded() {
        if (!packagesLoaded) {
            final AbstractModelLoader modelLoader = getModelLoader();
            modelLoader.synchronizedRun(new Runnable() {
                @Override
                public void run() {
                    if (!packagesLoaded) {
                        loadPackages(modelLoader);
                        packagesLoaded = true;
                    }
                }
            });
        }
    }

    /**
     * Loads all relevant packages for this module.
     * @param modelLoader the model loader to use for package creation
     */
    private void loadPackages(AbstractModelLoader modelLoader) {
        final String moduleName = getNameAsString();
        for (String pkg : getJarPackages()) {
            if (shouldIncludePackage(moduleName, pkg)) {
                modelLoader.findOrCreatePackage(ReflectionModule.this, pkg);
            }
        }
    }

    /**
     * Determines if a package should be included based on module name and package name.
     * @param moduleName the name of the module
     * @param pkg the package name
     * @return true if the package should be included, false otherwise
     */
    private boolean shouldIncludePackage(String moduleName, String pkg) {
        return !moduleName.equals(AbstractModelLoader.CEYLON_LANGUAGE)
                || pkg.startsWith(AbstractModelLoader.CEYLON_LANGUAGE);
    }
}