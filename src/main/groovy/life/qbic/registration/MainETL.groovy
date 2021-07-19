package life.qbic.registration

import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import groovy.util.logging.Log4j2
import life.qbic.datasets.parsers.DatasetParser
import life.qbic.registration.handler.DatasetParserHandler
import life.qbic.registration.handler.RegistrationException
import life.qbic.registration.handler.RegistrationHandler
import life.qbic.registration.handler.Registry
import life.qbic.utils.BioinformaticAnalysisParser
import life.qbic.utils.MaxQuantParser
import life.qbic.utils.NanoporeParser

import java.nio.file.Path

@Log4j2
class MainETL extends AbstractJavaDataSetRegistrationDropboxV2 {

    static List<DatasetParser<?>> listOfParsers = [
            new BioinformaticAnalysisParser(),
            new MaxQuantParser(),
            new NanoporeParser(),
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
            handler.getObservedExceptions().each {
                log.error(it.message, it.getStackTrace().join("\n"))
            }
            throw new Exception("Data structure could not be parsed.")
        })

        Registry registry =  RegistrationHandler.getRegistryFor(concreteResult).orElseThrow(
                {
                    throw new Exception("No registry found for data structure.")
                }
        )

        try {
            registry.executeRegistration(transaction, relevantData)
        } catch (RegistrationException e) {
            log.error(e.getMessage())
            throw new Exception("Could not register data! Manual intervention is needed.")
        }
    }
}

