package life.qbic.registration

import java.io.File
import java.util.ArrayList
import java.util.List
import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample
import life.qbic.datasets.parsers.DatasetParser
import life.qbic.datamodel.datasets.NfCorePipelineResult
import life.qbic.registration.handler.RegistrationHandler
import life.qbic.registration.handler.Registry
import life.qbic.utils.*
import groovy.util.logging.Log4j2
import java.nio.file.Path

@Log4j2
class MainETL extends AbstractJavaDataSetRegistrationDropboxV2 {

    static List<DatasetParser<?>> listOfParsers = [new BioinformaticAnalysisParser(), new NanoporeParser()] as List<DatasetParser<?>>

    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction) {
        String incomingPath = transaction.getIncoming().getAbsolutePath()
        String incomingName = transaction.getIncoming().getName()

        Path relevantData = new File(incomingPath, incomingName).toPath()
        
        //TODO
        //do something with datahandler/datamover metadata like checksums
        for (File file : new File(incomingPath).listFiles()) {
            if (!file.getPath().equals(relevantData)) {
                //TODO
            }
        }   
        
        DatasetParserHandler handler = new DatasetParserHandler(listOfParsers)
        Optional<?> result = handler.parseFrom(relevantData)

        Object concreteResult = result.orElseThrow({
            throw new Exception("Data structure could not be parsed.")
        })

        Registry registry =  RegistrationHandler.getRegistryFor(concreteResult).orElseThrow(
                {
                    throw new Exception("No registry found for data structure.")
                }
        )

        registry.executeRegistration(transaction)
    }
}

