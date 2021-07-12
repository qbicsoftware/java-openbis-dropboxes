package life.qbic.registration.registries

import groovy.util.logging.Log4j2

import java.nio.file.Path

/**
 * <p>A collection of useful methods that can be shared among the different registries.</p>
 *
 * @since 1.1.0
 */
@Log4j2
class Utils {

    /**
     * <p>Iterates through the lines of a file and extracts the sample codes.
     * The sample codes must be line separated.
     * All trailing whitespace will get trimmed.</p>
     * @param file the path to the sample id file
     * @return a list of sample ids
     * @since 1.1.0
     */
    static Optional<List<String>> parseSampleIdsFrom(Path file) {
        def sampleIds = []
        try {
            def fileRowEntries = new File(file.toUri()).readLines()
            for (String row : fileRowEntries) {
                sampleIds.add(row.trim())
            }
        } catch (Exception e) {
            switch (e) {
                case FileNotFoundException:
                    log.error "File ${file} was not found."
                    break
                default:
                    log.error "Could not read from file ${file}."
                    log.error "Reason: ${e.stackTrace.join("\n")}"
            }
        }
        return sampleIds ? Optional.of(sampleIds) : Optional.empty() as Optional<List<String>>
    }
}
