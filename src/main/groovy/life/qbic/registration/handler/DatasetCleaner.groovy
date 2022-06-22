package life.qbic.registration.handler

import java.nio.file.Path

/**
 * Removes unwanted files from a file structure
 *
 * @since 1.5.0
 */
interface DatasetCleaner {
    /**
     * <p>Tries to remove unwanted files from a file structure.<p>
     *
     * @param datasetRoot the top level path of the directory with the file structure to be cleaned.
     * @since 1.5.0
     */
    void clean(Path datasetRoot)

}
