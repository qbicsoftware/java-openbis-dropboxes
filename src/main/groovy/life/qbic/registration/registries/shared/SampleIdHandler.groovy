package life.qbic.registration.registries.shared

import groovy.util.logging.Log4j2
import life.qbic.registration.SampleId

import java.nio.file.Path

/**
 * <p>Provides central sample id handling logic<p>
 *
 * @since 1.0.0
 */
@Log4j2
class SampleIdHandler {
    /**
     * <p>Iterates through the lines of a file and extracts the sample codes.
     * The sample codes must be line separated.
     * All trailing whitespace will get trimmed.</p>
     * @param file the path to the sample id file
     * @return a list of sample ids
     * @since 1.1.0
     */
    static Optional<List<String>> parseSampleIdsFrom(Path file) {
        List<String> sampleIds = []
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

    /**
     * <p>Converts and intrinsically validates a list of String sample ids into a list of {@link life.qbic.registration.SampleId}.</p>
     * @param sampleIdList a list of sample ids
     * @return a list of converted and validated sample ids
     * @throws RuntimeException if at least one sample id cannot be converted
     * @since 1.1.0
     */
    static List<SampleId> convertSampleIds(List<String> sampleIdList) throws RuntimeException {
        def convertedSampleIds = []
        for(String sampleId : sampleIdList) {
            def convertedId = SampleId.from(sampleId).orElseThrow( {
                throw new RuntimeException("$sampleId does not seem to contain a valid sample id.")})
            convertedSampleIds.add(convertedId)
        }
        return convertedSampleIds
    }
}
