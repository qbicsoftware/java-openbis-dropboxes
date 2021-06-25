package life.qbic.registration

import spock.lang.Specification

/**
 * Tests for the {@link AnalysisResultId} class.
 *
 * @since 1.0.0
 */
class ExperimentIdSpec extends Specification {

    def "Parsing from a valid experiment id String should create an object"() {
        given: "A valid experiment id string"
        def validIdentifier = "QTESTE1"

        when: "we try to create a new object from the string"
        ExperimentId id = ExperimentId.parseFrom(validIdentifier)

        then:
        noExceptionThrown()
        id.id == 1
    }

    def "Parsing from a valid FULL experiment id String should create an object"() {
        given: "A valid experiment result id string"
        def validIdentifier = "/MNF_NORDHEIM_MURINEHCC/QANMX/QANMXE1"

        when: "we try to create a new object from the string"
        ExperimentId id = ExperimentId.parseFrom(validIdentifier)

        then:
        noExceptionThrown()
        id.id == 1
    }

    def "Parsing from a invalid result id String should throw a NumberFormatException"() {
        given: "An invalid analysis result id string"
        def validIdentifier = "/MNF_NORDHEIM_MURINEHCC/QANMX/QANMX_INFO"

        when: "we try to create a new object from the string"
        ExperimentId.parseFrom(validIdentifier)

        then:
        thrown(NumberFormatException)
    }

    def "Calling nextId should create a new experiment id with an increment of 1"() {
        given: "An invalid analysis result id string"
        def validIdentifier = "E1"

        when: "we try to create a new object from the string"
        def id = ExperimentId.parseFrom(validIdentifier)
        def nextId = id.nextId()


        then:
        assert nextId != id
        assert nextId.getId() == 2
    }

    def "Comparing an higher analysis result id, with a lower, shall return 1"() {
        given: "An id with a higher id value, and one with a lower"
        def higherId = ExperimentId.parseFrom("QTESTE10")
        def lowerId = ExperimentId.parseFrom("QTESTE2")

        when: "we compare the higher id with the lower id"
        int result = higherId <=> lowerId

        then: "the returned value must be 1"
        result == 1

    }

    def "Comparing an lower analysis result id, with a higher, shall return -1"() {
        given: "An id with a higher id value, and one with a lower"
        def higherId = ExperimentId.parseFrom("QTESTE10")
        def lowerId = ExperimentId.parseFrom("QTESTE2")

        when: "we compare the lower id with the higher id"
        int result = lowerId <=> higherId

        then: "the returned value must be 1"
        result == -1

    }

    def "Comparing two equal ids shall return 0"() {
        given: "Two ids with the same value"
        def higherId = ExperimentId.parseFrom("QTESTE10")
        def lowerId = ExperimentId.parseFrom("QTESTE10")

        when: "we compare them"
        int result = higherId <=> lowerId

        then: "the returned value must be 0"
        result == 0
    }

    def "Comparing with null throws null pointer exception"() {
        given: "An id"
        def id = ExperimentId.parseFrom("QTESTE10")

        when: "we compare it with null"
        id.compareTo(null)

        then: "a NPE must be thrown"
        thrown(NullPointerException)
    }



}
