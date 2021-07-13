package life.qbic.registration.registries

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
import groovy.util.logging.Log4j2
import life.qbic.datamodel.datasets.datastructure.folders.maxquant.MaxQuantRun
import life.qbic.registration.AnalysisResultId
import life.qbic.registration.Context
import life.qbic.registration.ExperimentId
import life.qbic.registration.SampleId
import life.qbic.registration.handler.RegistrationException
import life.qbic.registration.handler.Registry
import life.qbic.registration.types.QDatasetType
import life.qbic.registration.types.QExperimentType
import life.qbic.registration.types.QPropertyTypes
import life.qbic.registration.types.QSampleType

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

    private void register(IDataSetRegistrationTransactionV2 transaction, List<String> sampleIds) {
        // 1. Get the openBIS samples the datasets belong to
        // Will contain the openBIS samples which data served as input data for
        // the pipeline run
        List<SampleId> sampleIdList = Utils.validateSampleIds(sampleIds)

        Context context = Utils.getContext(sampleIdList[0], transaction.getSearchService()).orElseThrow({
            throw new RegistrationException(("Could not determine context for samples ${sampleIdList}"))
        })

        // We grep the openBIS samples, the analysis was based on
        List<ISample> parentSamples = []
        for (SampleId sampleId : sampleIdList) {
            ISample sample = transaction.getSampleForUpdate("/${context.getProjectSpace()}/$sampleId")
            parentSamples.add(sample)
        }
        // Both lists must have the same length,
        // otherwise that means that not all provided sample ids where found in openBIS
        assert sampleIdList.size() == parentSamples.size()

        // 2. Get existing analysis run results
        ISearchService searchService = transaction.getSearchService()
        SearchCriteria searchCriteriaResultSamples = new SearchCriteria()
        searchCriteriaResultSamples.addMatchClause(
                SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE,
                        "${sampleIdList[0].getProjectCode().toString()}R*")
        )

        List<ISampleImmutable> existingAnalysisResultSamples = searchService.searchForSamples(searchCriteriaResultSamples)

        List<AnalysisResultId> existingAnalysisRunIds = []
        for (sample in existingAnalysisResultSamples) {
            AnalysisResultId id = AnalysisResultId.parseFrom(sample.code)
            existingAnalysisRunIds.add(id)
        }
        existingAnalysisRunIds.sort(Comparator.naturalOrder())

        // 3. Get existing experiments
        List<IExperimentImmutable> existingExperiments =
                searchService.listExperiments("/${context.getProjectSpace().toString()}/${sampleIdList[0].getProjectCode().toString()}") as List<IExperimentImmutable>
        List<ExperimentId> existingExperimentIds = []
        for (experiment in existingExperiments) {
            try {
                ExperimentId id = ExperimentId.parseFrom(experiment.experimentIdentifier)
                existingExperimentIds.add(id)
            } catch (NumberFormatException e) {
                log.error "Cannot process experiment with id ${experiment.getExperimentIdentifier()}"
            }
        }
        existingExperimentIds.sort(Comparator.naturalOrder())

        // 4. Create new run result sample
        def newAnalysisRunId = existingAnalysisRunIds ? existingAnalysisRunIds.last().nextId() : new AnalysisResultId(1)
        // New sample code /<space>/<project code>R<number>
        def newRunSampleId = "/${context.getProjectSpace().toString()}/${context.getProjectCode().toString()}${newAnalysisRunId.toString()}"
        def newOpenBisSample = transaction.createNewSample(newRunSampleId, QSampleType.Q_WF_MS_MAXQUANT_RUN as String)


        // 5. Create new experiment
        ExperimentId newExperimentId = existingExperimentIds ? existingExperimentIds.last().nextId() : new ExperimentId(1)
        // New sample code /<space>/<project code>/<project code>E<number>
        def newExperimentFullId = "/${context.getProjectSpace().toString()}/" +
                "${context.getProjectCode().toString()}/" +
                "${context.getProjectCode().toString()}${newExperimentId.toString()}"

        def newExperiment = transaction.createNewExperiment(newExperimentFullId, QExperimentType.Q_WF_MS_MAXQUANT as String)
        // TODO: still not dynamically filled, as at the time of writing, there is no input available yet.
        newExperiment.setPropertyValue(QPropertyTypes.Q_WF_NAME.toString(), "MaxQuant MS Analysis")

        // 6. Set parent samples as parents in the newly created run result sample
        newOpenBisSample.setParentSampleIdentifiers(sampleIds)

        // 7. Set experiment for run result sample
        newOpenBisSample.setExperiment(newExperiment)

        // 8. Create new openBIS dataset
        def dataset = transaction.createNewDataSet(QDatasetType.Q_WF_MS_MAXQUANT_RESULTS as String)
        dataset.setSample(newOpenBisSample)

        // 9. Attach result data to dataset
        transaction.moveFile(this.datasetRootPath.toString(), dataset)
    }
}
