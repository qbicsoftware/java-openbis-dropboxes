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
        id.projectCode.toString() == "QTEST"
    }

    def "Creating a sample id object from a text containing two valid ids must return the first valid sampleId"() {
        given: "A text with two valid sample ids"
        def validId = " QREST010BB or maybe QTEST001AE"

        when: "We parse a sample id from the text"
        Optional<SampleId> sampleId = SampleId.from(validId)

        then: "The id must be the first valid occurrence of a sample id in the text"
        SampleId id = sampleId.get()
        id.runningNumber == 10
        id.projectCode.toString() == "QREST"
    }

    def "Creating a sample id object from a text containing no valid ids must return an empty object"() {
        given: "A text with two invalid sample ids"
        def invalidIds = " QREST01 or maybe QTEST"

        when: "We parse a sample id from the text"
        Optional<SampleId> sampleId = SampleId.from(invalidIds)

        then: "The id must be empty"
        !sampleId.isPresent()
    }

    def "Ensure equality check works"() {
        given: "Two sample Ids with the same content, and one with different content"
        def oneSampleId = SampleId.from("QTEST001AE")
        def sameSampleId = SampleId.from("QTEST001AE")
        def otherSampleId = SampleId.from("QVARA001AE")

        when: "we compare them"
        boolean sameSamplesEqual = oneSampleId.equals(sameSampleId)
        boolean differentSamplesEqual = otherSampleId.equals(oneSampleId)

        then: "must evaluate as such during comparison"
        sameSamplesEqual
        !differentSamplesEqual
    }

    def "Ensure toString method produces expected format"() {
        given: "A sample id"
        def sampleId = SampleId.from("QTEST001AE")

        when: "we call the toString() method"
        String result = sampleId.get().toString()

        then: "we verify the format to be as expected"
        result == "QTEST001AE"
    }
}
