package life.qbic.registration

import groovy.transform.EqualsAndHashCode

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A sample identifier references an openBIS sample.
 *
 * @since 1.0.0
 */
@EqualsAndHashCode
class SampleId {

    /**
     * <p>The regular expression of a valid QBiC sample code.</p>
     */
    static final Pattern QBIC_SAMPLE_BARCODE_SCHEMA = ~/Q[A-X0-9]{4}[0-9]{3}[A-X0-9]{2}/

    /**
     * <p>Holds the project id, which is always part of the sample id.</p>
     */
    final String projectCode

    /**
     * <p>Holds the running sample number.</p>
     *
     * <p>For example if the sample id is <code>QTEST001AE</code> then the running number is <code>1</code>.</p>
     */
    final Integer runningNumber

    /**
     * <p>Parses a sample identifier from a provided text.</p>
     *
     * <p>If the provided text provides more than one valid sample identifier, only the first one
     * will be returned.</p>
     *
     * <p>If no (valid) sample id was found, the returned {@link Optional} will be empty.</p>
     *
     * @param text the text to search for a valid sample identifier.
     * @return The sample id or empty.
     */
    static Optional<SampleId> from(String text) {
        Optional<SampleId> sampleId = Optional.empty()

        Matcher matcher = text =~ QBIC_SAMPLE_BARCODE_SCHEMA
        if (matcher.find()) {
            sampleId = Optional.of(new SampleId(matcher[0] as String))
        }
        return sampleId
    }

    private SampleId() {
    }

    private SampleId(String sampleId) {
        this.projectCode = sampleId[0..4]
        this.runningNumber = Integer.parseInt(sampleId[5..7])
    }

}
