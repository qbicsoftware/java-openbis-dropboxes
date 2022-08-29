package life.qbic.registration

import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import groovy.util.logging.Log4j2
import life.qbic.datasets.parsers.DataParserException
import life.qbic.datasets.parsers.DatasetParser
import life.qbic.datasets.parsers.DatasetValidationException
import life.qbic.registration.handler.*
import life.qbic.utils.BioinformaticAnalysisParser
import life.qbic.utils.MaxQuantParser

import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Consumer

@Log4j2
class MainETL extends AbstractJavaDataSetRegistrationDropboxV2 {

    static List<DatasetParser<?>> listOfParsers = [
            new MaxQuantParser(),
            new BioinformaticAnalysisParser()
    ] as List<DatasetParser<?>>

    @Override
    void process(IDataSetRegistrationTransactionV2 transaction) {
        String incomingPath = transaction.getIncoming().getAbsolutePath()
        String incomingName = transaction.getIncoming().getName()
        String relevantData = new File(incomingPath, incomingName).getAbsolutePath()

        DatasetLocator locator = DatasetLocatorImpl.of(relevantData)
        String pathToDatasetFolder = locator.getPathToDatasetFolder()
        log.info("Incoming dataset '$relevantData'")
        log.info("Identified dataset location in '${pathToDatasetFolder}'...")

        if (isTarArchive(pathToDatasetFolder)) {
            log.info("Found TAR archive dataset")
            pathToDatasetFolder = extractTar(pathToDatasetFolder)
        }

        DatasetParserHandler handler = new DatasetParserHandler(listOfParsers)
        Optional<?> result = handler.parseFrom(Paths.get(pathToDatasetFolder))

        Object concreteResult = result.orElseThrow({
            logExceptionReport(handler.getObservedExceptions())
            throw new RegistrationException("Data structure could not be parsed.")
        })

        DatasetCleaner cleaner = new HiddenFilesCleaner()
        try {
            cleaner.clean(Paths.get(pathToDatasetFolder))
        } catch (IOException e) {
            log.error(e.getMessage())
            throw new RegistrationException("Error when deleting hidden file.")
        }

        Registry registry =  RegistrationHandler.getRegistryFor(concreteResult).orElseThrow(
                {
                    throw new RegistrationException("No registry found for data structure.")
                }
        )

        try {
            registry.executeRegistration(transaction, Paths.get(pathToDatasetFolder))
        } catch (RegistrationException e) {
            log.error(e.getMessage())
            throw new RegistrationException("Could not register data! Manual intervention is needed.")
        }
    }

    private static isTarArchive(String dataset) {
        try {
            new TarArchive(Paths.get(dataset))
            return true
        } catch (IllegalArgumentException ignored) {
            return false
        }
    }

    private static logExceptionReport(Map<String, Exception> observedExceptions) {
        log.error("Detailed exception report:")
        log.error("start---------------------")
        for (String parser : observedExceptions.keySet()) {
            log.error("Report for parser: " + parser)
            Exception exception = observedExceptions.get(parser)
            switch (exception) {
                case DataParserException:
                    log.error(exception.getMessage())
                    break
                case DatasetValidationException:
                    logDatasetValidations(exception as DatasetValidationException)
                    break
                default:
                    log.error(exception.stackTrace.join("\n"))
            }
            log.error("######")
        }
        log.error("end-----------------------")
    }

    private static logDatasetValidations(DatasetValidationException exception) {
        for(String message : exception.getAllExceptions()) {
            log.error("Validation exception: ${message}.")
        }
    }

    private static String extractTar(String datasetPath) {
        Path dataset = Paths.get(datasetPath)
        TarArchive archive = new TarArchive(dataset)
        Path parentDir = dataset.getParent()
        Path extractionDir = Paths.get(parentDir.toAbsolutePath().toString(), "tmp_extraction")
        log.info("Extracting tar archive content to: " + extractionDir.toAbsolutePath().toString())

        // creates the destination directory for extraction
        new File(extractionDir.toAbsolutePath().toString()).mkdir()

        TarArchiveHandler.extract(archive, extractionDir, new Consumer<TarExtractionResult>() {
            @Override
            void accept(TarExtractionResult tarExtractionResult) {
                log.info("Extracted tar archive " + tarExtractionResult.archive().name() + " successfully")
            }
        }, new Consumer<TarExtractionFailure>() {
            @Override
            void accept(TarExtractionFailure tarExtractionFailure) {
                log.error(tarExtractionFailure.description())
                throw new RegistrationException("Unpacking tar archive failed!",)
            }
        })
        return extractionDir
    }
}
