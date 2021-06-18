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
}
