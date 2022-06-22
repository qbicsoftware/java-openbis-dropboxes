package life.qbic.registration.handler

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class HiddenFilesCleaner implements DatasetCleaner {


    /**
     * <p>Tries to remove unwanted, hidden files from a file structure.<p>
     *
     * @param datasetRoot the top level path of the directory with the file structure to be parsed.
     * @since 1.5.0
     */
    @Override
    void clean(Path datasetRoot) {
            Files.walk(datasetRoot).forEach(path -> {
                if(path.toFile().isHidden()) {
                    path.toFile().delete()
                }
            })
    }
}
