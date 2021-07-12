package life.qbic.registration.registries

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import groovy.util.logging.Log4j2
import life.qbic.datamodel.datasets.datastructure.folders.maxquant.MaxQuantRun
import life.qbic.registration.handler.RegistrationException
import life.qbic.registration.handler.Registry

import java.nio.file.Path

/**
 * <p>Registration of MaxQuant analysis result datasets.</p>
 *
 * <p>This registry is supposed to register a MaxQuant analysis result dataset in openBIS,
 * based on a pre-defined model (please checkout the README for the detailed model).</p>
 *
 * @since 1.1.0
 */
@Log4j2
class MaxQuantResultRegistry implements Registry{

    private Path datasetRootPath

    private final MaxQuantRun maxQuantRun

    /**
     * Creates MaxQuantResultRegistry instance, given a MaxQuant result dataset.
     * @param maxQuantRun the MaxQuant result dataset
     * @since 1.1.0
     */
    MaxQuantResultRegistry(MaxQuantRun maxQuantRun) {
        this.maxQuantRun = maxQuantRun
    }

    /**
     * {@inheritDocs}
     */
    @Override
    void executeRegistration(IDataSetRegistrationTransactionV2 transaction, Path datasetRootPath) throws RegistrationException {
        this.datasetRootPath = datasetRootPath

        def sampleIds = getInputSamples().orElseThrow({
            throw new RegistrationException("Could not determine sample codes that have been used" +
                    "to perform the MaxQuant analysis.")
        })

        try {
            register(transaction, sampleIds)
        } catch (RuntimeException e) {
            log.error("An exception occurred during the registration.")
            throw new RegistrationException(e.getMessage())
        }
    }

    private Optional<List<String>> getInputSamples() {
        def sampleIdPath = Paths.get(datasetRootPath.toString(), maxQuantRun.sampleIds.relativePath)
        return Utils.parseSampleIdsFrom(sampleIdPath)
    }

    private void register(IDataSetRegistrationTransactionV2 transactionV2, List<String> sampleIds) {
        // Todo implement registration in openBIS
    }
}
