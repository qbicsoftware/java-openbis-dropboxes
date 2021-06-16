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
class MainETL extends AbstractJavaDataSetRegistrationDropboxV2{

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
        
        List<DatasetParser<?>> listOfParsers = new ArrayList<>()
        
        listOfParsers.add(new NanoporeParser())
        listOfParsers.add(new BioinformaticAnalysisParser())
        
        DatasetParserHandler handler = new DatasetParserHandler(listOfParsers)
        Optional<?> result = handler.parseFrom(relevantData)
        Optional<Registry> registry = RegistrationHandler.getRegistryFor(result)
        
        if(registry.isPresent()) {
            registry.get().executeRegistration(transaction)
        } else {
            log.error("No registry found for data structure.")
        }
        //ISample sample = transaction.getSampleForUpdate("/TEST28/QXEGD018AW")
        //IDataSet dataSet = transaction.createNewDataSet()
        //dataSet.setSample(sample)
        //transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet as IDataSet)
    }
}