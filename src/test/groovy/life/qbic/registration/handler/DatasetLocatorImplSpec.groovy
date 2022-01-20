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

    def "Given a relative path, when a DatasetLocator is created then an IllegalArgumentException is thrown"() {
        when:
        DatasetLocator locator = DatasetLocatorImpl.of(relativePathExample)

        then:
        thrown(IllegalArgumentException.class)
    }

    def "Given an absolute path, when a DatasetLocator is created then an instance of this class is returned"() {
        when:
        DatasetLocator locator = DatasetLocatorImpl.of(nestedDataStructureExample)

        then:
        noExceptionThrown()
    }

    def "Given a nested structure, when the path to the dataset folder is requested, return the absolute path to the parent folder"() {
        given:
        DatasetLocator locator = DatasetLocatorImpl.of(nestedDataStructureExample)

        when:
        String datasetPath = locator.getPathToDatasetFolder()

        then:
        datasetPath != nestedDataStructureExample

        new File(datasetPath).getParent().equalsIgnoreCase(nestedDataStructureExample)
    }

    def "Given a flat structure, when the path to the dataset folder is requested, return the given path"() {
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
