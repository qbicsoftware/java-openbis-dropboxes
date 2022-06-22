package life.qbic.registration.handler

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class HiddenFilesCleanerSpec extends Specification {

    def "Give a folder structure with and without hidden files, the hidden files cleaner removes only hidden files"() {
        given: "A DatasetCleaner that removes Hidden Files"
        DatasetCleaner cleaner = new HiddenFilesCleaner()
        List<File> hiddenFiles = new ArrayList<>()
        List<File> visibleFiles = new ArrayList<>()

        Path tempDirPath = Files.createTempDirectory("20220615101241_maxquant_results_QABCDR03R14_Exp1Phospho")
        File tempDir = tempDirPath.toFile()

        hiddenFiles.add(File.createTempFile("._QABCD_sample_ids", ".txt", tempDir))
        hiddenFiles.add(File.createTempFile("._.DS_Store", "", tempDir))
        hiddenFiles.add(File.createTempFile(".DS_Store", "", tempDir))

        visibleFiles.add(tempDir)
        visibleFiles.add(File.createTempFile("mqpar", ".xml", tempDir))
        visibleFiles.add(File.createTempFile("QABCD_sample_ids", ".txt", tempDir))

        Path level2Path = Files.createTempDirectory(tempDirPath,"txt")
        File level2 = level2Path.toFile()
        visibleFiles.add(level2)
        visibleFiles.add(File.createTempFile("allPeptides", ".txt", level2))
        visibleFiles.add(File.createTempFile("evidence", ".txt", level2))
        visibleFiles.add(File.createTempFile("experimentalDesignTemplate", ".txt", level2))
        visibleFiles.add(File.createTempFile("libraryMatch", ".txt", level2))
        visibleFiles.add(File.createTempFile("matchedFeatures", ".txt", level2))
        visibleFiles.add(File.createTempFile("mzTab", ".mzTab", level2))
        visibleFiles.add(File.createTempFile("peptides", ".txt", level2))
        visibleFiles.add(File.createTempFile("proteinGroups", ".txt", level2))
        visibleFiles.add(File.createTempFile("summary", ".txt", level2))
        visibleFiles.add(File.createTempFile("tables", ".pdf", level2))

        File level3 = Files.createTempDirectory(level2Path, "summary").toFile()
        visibleFiles.add(File.createTempFile("PhosphoSites_nonredundant", ".txt", level3))
        visibleFiles.add(level3)

        when:
        cleaner.clean(tempDir.toPath())

        then: "the result must not contain the hidden file, but all other files"
        for (File hidden : hiddenFiles) {
            assert !hidden.exists()
        }
        for (File visible : visibleFiles) {
            assert visible.exists()
        }
    }
}
