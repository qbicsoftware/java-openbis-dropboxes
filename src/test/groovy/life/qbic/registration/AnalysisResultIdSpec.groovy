package life.qbic.registration

import spock.lang.Specification

/**
 * Tests for the {@link AnalysisResultId} class.
 *
 * @since 1.0.0
 */
class AnalysisResultIdSpec extends Specification {

    def "Parsing from a valid result id String should create an object"() {
        given: "A valid analysis result id string"
        def validIdentifier = "QTESTR1"

        when: "we try to create a new object from the string"
        AnalysisResultId id = AnalysisResultId.parseFrom(validIdentifier)

        then:
        noExceptionThrown()
        id.id == 1
    }

    def "Parsing from a invalid result id String should throw a NumberFormatException"() {
        given: "An invalid analysis result id string"
        def validIdentifier = "QTESTRf3"

        when: "we try to create a new object from the string"
        AnalysisResultId.parseFrom(validIdentifier)

        then:
        thrown(NumberFormatException)
    }

    def "Comparing an higher analysis result id, with a lower, shall return 1"() {
        given: "An id with a higher id value, and one with a lower"
        def higherId = AnalysisResultId.parseFrom("QTESTR10")
        def lowerId = AnalysisResultId.parseFrom("QTESTR2")

        when: "we compare the higher id with the lower id"
        int result = higherId <=> lowerId

        then: "the returned value must be 1"
        result == 1

    }

    def "Comparing an lower analysis result id, with a higher, shall return -1"() {
        given: "An id with a higher id value, and one with a lower"
        def higherId = AnalysisResultId.parseFrom("QTESTR10")
        def lowerId = AnalysisResultId.parseFrom("QTESTR2")

        when: "we compare the lower id with the higher id"
        int result = lowerId <=> higherId

        then: "the returned value must be 1"
        result == -1

    }

    def "Comparing two equal ids shall return 0"() {
        given: "Two ids with the same value"
        def higherId = AnalysisResultId.parseFrom("QTESTR10")
        def lowerId = AnalysisResultId.parseFrom("QTESTR10")

        when: "we compare them"
        int result = higherId <=> lowerId

        then: "the returned value must be 0"
        result == 0
    }
}
