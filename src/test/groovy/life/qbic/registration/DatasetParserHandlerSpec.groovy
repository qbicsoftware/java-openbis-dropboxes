package life.qbic.registration

import life.qbic.datasets.parsers.DataParserException
import life.qbic.datasets.parsers.DatasetParser
import life.qbic.datasets.parsers.DatasetValidationException
import life.qbic.registration.DatasetParserHandler
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Tests for the parser handler.
 *
 * @since 1.0.0
 */
class DatasetParserHandlerSpec extends Specification {

    def "Creating an parser handler instance of a list of parsers shall throw no exception"() {
        given: "an implementation of the DatasetParser interface"
        DatasetParser<String> failingParser = new DatasetParser<String>() {
            @Override
            String parseFrom(Path path) throws DataParserException, DatasetValidationException {
                throw new DataParserException("Cannot determine dataset type.")
            }
        }

        when: "an instance of the DatasetParserHandler is created"
        DatasetParserHandler handler = new DatasetParserHandler([failingParser])

        then: "no exceptions are thrown"
        noExceptionThrown()
    }


    def "If no parser is able to determine the dataset type, the optional return shall be empty"() {
        given: "a parser that fails always"
        DatasetParser<String> failingParser = new DatasetParser<String>() {
            @Override
            String parseFrom(Path path) throws DataParserException, DatasetValidationException {
                throw new DataParserException("Cannot determine dataset type.")
            }
        }

        and: "a parser handler with that failing parser"
        DatasetParserHandler handler = new DatasetParserHandler([failingParser])

        when: "we trigger the parser"
        Optional result = handler.parseFrom(Paths.get("./"))

        then: "the result must be empty"
        !result.isPresent()

    }

    def "If a parser is able to determine the dataset type, the optional return contain the parsed dataset"() {
        given: "a simple parser that will determine the dataset"
        DatasetParser<String> simpleParser = new DatasetParser<String>() {
            @Override
            String parseFrom(Path path) throws DataParserException, DatasetValidationException {
                "Text dataset"
            }
        }

        and: "a parser handler with that simple parser"
        DatasetParserHandler handler = new DatasetParserHandler([simpleParser])

        when: "we trigger the parser"
        Optional result = handler.parseFrom(Paths.get("./"))

        then: "the result must contain the String dataset"
        assert result.isPresent()
        assert result.get() instanceof String
        assert result.get() as String == "Text dataset"

    }


}
