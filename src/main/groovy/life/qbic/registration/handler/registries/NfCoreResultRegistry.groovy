package life.qbic.registration.handler.registries

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import life.qbic.registration.handler.RegistrationException
import life.qbic.registration.handler.Registry

/**
 * <p>Registers nf-core pipeline results to openBIS.</p>
 * <br>
 * <p>The output nf-core bioinformatic pipeline produce is complex. There are folders
 * that hold information about the individual workflow steps (aka process folders), there is
 * general information about quality control and there is information about the sample codes that
 * reflect the input datasets for the analysis.</p>
 * <br>
 *
 * @since 1.0.0
 */
class NfCoreResultRegistry implements Registry {

    /**
     * {@inheritDocs}
     */
    @Override
    void executeRegistration(IDataSetRegistrationTransactionV2 transaction) throws RegistrationException {

    }
}
