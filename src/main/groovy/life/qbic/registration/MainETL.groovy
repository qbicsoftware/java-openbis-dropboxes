package life.qbic.registration

import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import groovy.util.logging.Log4j2
import life.qbic.datasets.parsers.DatasetParser
import life.qbic.datasets.parsers.DataParserException
import life.qbic.datasets.parsers.DatasetValidationException
import life.qbic.registration.handler.DatasetParserHandler
import life.qbic.registration.handler.RegistrationException
import life.qbic.registration.handler.RegistrationHandler
import life.qbic.registration.handler.Registry
import life.qbic.utils.BioinformaticAnalysisParser
import life.qbic.utils.MaxQuantParser

import java.nio.file.Path

@Log4j2
class MainETL extends AbstractJavaDataSetRegistrationDropboxV2 {

    static List<DatasetParser<?>> listOfParsers = [
            new BioinformaticAnalysisParser(),
            new MaxQuantParser()
    ] as List<DatasetParser<?>>

    @Override
    void process(IDataSetRegistrationTransactionV2 transaction) {
        String incomingPath = transaction.getIncoming().getAbsolutePath()
        String incomingName = transaction.getIncoming().getName()

        Path relevantData = new File(incomingPath, incomingName).toPath()
        log.info("Processing incoming dataset '${relevantData.toString()}'...")

        DatasetParserHandler handler = new DatasetParserHandler(listOfParsers)
        Optional<?> result = handler.parseFrom(relevantData)

        Object concreteResult = result.orElseThrow({
            logExceptionReport(handler.getObservedExceptions())
            throw new RegistrationException("Data structure could not be parsed.")
        })

        Registry registry =  RegistrationHandler.getRegistryFor(concreteResult).orElseThrow(
                {
                    throw new RegistrationException("No registry found for data structure.")
                }
        )

        try {
            registry.executeRegistration(transaction, relevantData)
        } catch (RegistrationException e) {
            log.error(e.getMessage())
            throw new RegistrationException("Could not register data! Manual intervention is needed.")
        }
    }

    private static logExceptionReport(List<Exception> observedExceptions) {
        log.error("Detailed exception report:")
        log.error("start---------------------")
        for (Exception exception : observedExceptions) {
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
        }
        log.error("end-----------------------")
    }

    private static logDatasetValidations(DatasetValidationException exception) {
        for(String message : exception.allExceptions) {
            log.error("Validation exception: ${message}.")
        }
    }
}
