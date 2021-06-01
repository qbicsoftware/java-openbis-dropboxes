package life.qbic.registration

import spock.lang.Specification

/**
 * Tests for the {@link SampleId} class.
 *
 * @since 1.0.0
 */
class SampleIdSpec extends Specification {

    def "Creating a sample id object from a text containing a valid id must create a valid sampleId object"() {
        given: "A text with one valid sample id"
        def validId = "QTEST001AE"

        when: "We parse a sample id from the text"
        Optional<SampleId> sampleId = SampleId.from(validId)

        then: "The id must not be empty"
        sampleId.isPresent()
        SampleId id = sampleId.get()
        id.runningNumber == 1
        id.projectCode == "QTEST"
    }
}
