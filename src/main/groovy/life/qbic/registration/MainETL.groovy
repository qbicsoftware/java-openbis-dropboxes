package life.qbic.registration

import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample

class MainETL extends AbstractJavaDataSetRegistrationDropboxV2{

    @Override
    void process(IDataSetRegistrationTransactionV2 transaction) {
        ISample sample = transaction.getSampleForUpdate("/TEST28/QXEGD018AW")
        IDataSet dataSet = transaction.createNewDataSet()
        dataSet.setSample(sample)
        transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet as IDataSet)
    }
}