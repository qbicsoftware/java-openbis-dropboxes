package life.qbic.registration

import life.qbic.datasets.parsers.DataParserException
import life.qbic.datasets.parsers.DataSetParser
import life.qbic.datasets.parsers.DataSetValidationException

import java.nio.file.Path

/**
 * Handler for parsing file structures into known datasets.
 *
 * Since for a machine it is impossible to know which dataset
 * is given without a machine-readable description of datasets and
 * their representation, this handler helps to determine the dataset type
 * and convert it to a typed object the become lord of the data again.
 *
 * @since 1.0.0
 */
class ParserHandler {
    /*
    This list contains all registered dataset parser. When the dataset needs
    to be determined by a given file structure, the parser available in
    this list will be used to try the actual parsing.
     */
    private final List<DataSetParser<?>> dataSetParserList

    /**
     * Creates an instance of a parser handler object with a given list of
     * available {@link DataSetParser}.
     * @param listOfParsers a collection of {@link DataSetParser} to use for brute force parsing
     * of file structures.
     */
    ParserHandler(List<DataSetParser<?>> listOfParsers) {
        Objects.requireNonNull(listOfParsers, "List must not be null.")
        dataSetParserList = listOfParsers
    }

    /**
     * Tries to parse a given file structure into a dataset.
     *
     * The return type is not known during compile time, because
     * this method brute forces a file structure with different
     * available parsers, to determine the dataset type.
     *
     * The return <code>Optional</code> object is empty, if no registered parser was able to
     * determine the dataset successfully.
     *
     * If the return <code>Optional</code> contains a value, you need to determine the type by
     * Java's explicit type conversion in order to make use of static typing in any
     * downstream code that consumes dataset objects.
     *
     * @param root the top level path of the directory with the file structure to be parsed.
     * @return empty, if no parser was able to parse the file structure to a dataset. Else, the
     * object contains the dataset, that needs to be determined during runtime.
     * @since 1.0.0
     */
    Optional<?> parseFrom(Path root) {
        Iterator dataSetIterator = dataSetParserList.iterator()
        Optional<?> result = Optional.empty()
        while (dataSetIterator.hasNext()) {
            try {
                def parser = dataSetIterator.next()
                result = Optional.of(parser.parseFrom(root))
            } catch (DataParserException e) {
                // log it maybe
            } catch (DataSetValidationException e){
                // log it maybe
            }
            if (result.isPresent())
                return result
        }
        return result
    }
}
