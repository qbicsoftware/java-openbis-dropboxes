package life.qbic.registration.handler

/**
 * <b>DatasetLocatorImpl Class</b>
 *
 * <p>Finds nested datasets.</p>
 *
 * @since 1.3.0
 */
class DatasetLocatorImpl implements DatasetLocator{

    private final String rootPath

    /**
     * Creates an instance of a {@link DatasetLocator}.
     * @param rootPath - must be an absolute path to the top level directory of the data-structure under investigation
     */
    static of(String rootPath) {
        if (!rootPath || !isAbsolutePath(rootPath)) {
            throw new IllegalArgumentException("You must provide an absolute path. Provided: $rootPath")
        }
        return new DatasetLocatorImpl(rootPath)
    }

    private DatasetLocatorImpl(String rootPath) {
        this.rootPath = rootPath
    }

    private DatasetLocatorImpl(){}

    /**
     * @inheritDocs
     */
    @Override
    String getPathToDatasetFolder() {
        return findNestedDataset().orElseGet(() -> rootPath)
    }

    private Optional<String> findNestedDataset() {
        File rootDir = new File(rootPath)
        String dirName = rootDir.getName()
        /*
        Now we iterate through the list of child elements in the folder, and search for the directory name appearances.
        If it is a child directory, that means it has been processed with the dropboxhandler and
        we can return the nested dataset path.
        */
        for (String child : rootDir.list()) {
            if (child.equalsIgnoreCase(dirName)) {
                return Optional.of(rootDir.getAbsolutePath() + "/" + child)
            }
        }
        return Optional.empty()
    }

    private static boolean isAbsolutePath(String path) {
        return new File(path).isAbsolute()
    }
}
