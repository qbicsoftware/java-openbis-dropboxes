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

    def "Creating a sample id object from a text containing two valid ids must return the first valid sampleId"() {
        given: "A text with two valid sample ids"
        def validId = " QREST010BB or maybe QTEST001AE"

        when: "We parse a sample id from the text"
        Optional<SampleId> sampleId = SampleId.from(validId)

        then: "The id must be the first valid occurrence of a sample id in the text"
        SampleId id = sampleId.get()
        id.runningNumber == 10
        id.projectCode == "QREST"
    }

    def "Creating a sample id object from a text containing no valid ids must return an empty object"() {
        given: "A text with two invalid sample ids"
        def invalidIds = " QREST01 or maybe QTEST"

        when: "We parse a sample id from the text"
        Optional<SampleId> sampleId = SampleId.from(invalidIds)

        then: "The id must be empty"
        !sampleId.isPresent()
    }
}
