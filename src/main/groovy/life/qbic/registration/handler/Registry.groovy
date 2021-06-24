package life.qbic.registration.handler

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2

import java.nio.file.Path

/**
 * A registry that takes a dataset and registers it in openBIS.
 *
 * @since 1.0.0
 */
interface Registry {
    /**
     * <p>Clients that execute this method trigger an registration
     * of a dataset in openBIS.</p>
     * <br>
     * <p>Clients can expect that the registration was successful if no
     * {@link RegistrationException} was thrown.</p>
     * <br>
     * <p>Implementing classes must guarantee that a {@link RegistrationException} is thrown if any
     * exception occurs during the registration procedure.
     *
     * @param transaction an instance of an openBIS transaction event.
     * @param datasetRootPath the data set root path, must be an absolute path
     * @throws RegistrationException if the registration failed for any reason.
     * @since 1.0.0
     */
    void executeRegistration(IDataSetRegistrationTransactionV2 transaction,
                             Path datasetRootPath) throws RegistrationException

}
