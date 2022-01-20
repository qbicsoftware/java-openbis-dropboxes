package life.qbic.registration.handler

import spock.lang.Shared
import spock.lang.Specification

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
class DatasetLocatorImplSpec extends Specification {

    @Shared
    String nestedDataStructureExample

    @Shared
    String relativePathExample

    @Shared
    String flatDataStructureExample

    def setup() {
        setupNestedDataset()
        setupRelativePathExample()
        setupFlatDatasetExample()
    }

    def "Non absolute path shall return an IllegalArgumentException"() {
        when:
        DatasetLocator locator = DatasetLocatorImpl.of(relativePathExample)

        then:
        thrown(IllegalArgumentException.class)
    }

    def "Given an absolute path shall successfully create an instance of this class"() {
        when:
        DatasetLocator locator = DatasetLocatorImpl.of(nestedDataStructureExample)

        then:
        noExceptionThrown()
    }

    def "Given an nested structure, return the actual dataset absolute path"() {
        given:
        DatasetLocator locator = DatasetLocatorImpl.of(nestedDataStructureExample)

        when:
        String datasetPath = locator.getPathToDatasetFolder()

        then:
        datasetPath != nestedDataStructureExample

        new File(datasetPath).getParent().equalsIgnoreCase(nestedDataStructureExample)
    }

    def "Given an flat structure, return the actual root dataset absolute path"() {
        given:
        DatasetLocator locator = DatasetLocatorImpl.of(flatDataStructureExample)

        when:
        String datasetPath = locator.getPathToDatasetFolder()

        then:
        datasetPath == flatDataStructureExample
    }

    def setupNestedDataset() {
        File file = File.createTempDir()
        new File(file.getAbsolutePath() + File.separator + file.getName()).createNewFile()
        nestedDataStructureExample = file.getAbsolutePath()
    }

    def setupRelativePathExample() {
        File file = File.createTempDir()
        relativePathExample = file.getName()
    }

    def setupFlatDatasetExample() {
        File file = File.createTempDir()
        new File(file.getAbsolutePath() + File.separator + "myAwesomeDataset").createNewFile()
        flatDataStructureExample = file.getAbsolutePath()
    }

}
