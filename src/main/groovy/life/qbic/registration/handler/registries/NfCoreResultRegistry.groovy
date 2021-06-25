package life.qbic.registration.handler.registries

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
import groovy.util.logging.Log4j2
import life.qbic.datamodel.datasets.NfCorePipelineResult
import life.qbic.datamodel.dtos.projectmanagement.ProjectCode
import life.qbic.datamodel.dtos.projectmanagement.ProjectSpace
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
import java.nio.file.Paths

/**
 * <p>Registers nf-core pipeline results to openBIS.</p>
 * <br>
 * <p>The output nf-core bioinformatic pipeline produce is complex. There are folders
 * that hold information about the individual workflow steps (aka process folders), there is
 * general information about quality control and there is information about the sample codes that
 * reflect the input datasets for the analysis.</p>
 * <br>
 * <p>Since this registry makes some authenticated API requests against the Workflow Management System
 * Nextflow Tower, this registry expects to</p>
 *
 * @since 1.0.0
 */
@Log4j2
class NfCoreResultRegistry implements Registry {

    private static enum SampleType {
        ANALYSIS_WORKFLOW_RESULT
    }

    private static enum DataSetType {
        NF_CORE_RESULT
    }

    private static final Map<String, QExperimentType> PIPELINE_TO_EXPERIMENT_TYPE
    static {
        Map<String, QExperimentType> tmpMap = new HashMap<>()
        tmpMap.put("nf-core/atacseq", QExperimentType.Q_WF_NGS_ATAC_SEQ)
        tmpMap.put("nf-core/ampliseq", QExperimentType.Q_WF_NGS_16S_AMPLICON_SEQ)
        tmpMap.put("nf-core/bcellmacgic", QExperimentType.Q_WF_NGS_BCELL_REPERTOIRE_SEQ)
        tmpMap.put("nf-core/chipseq", QExperimentType.Q_WF_NGS_CHIP_SEQ)
        tmpMap.put("nf-core/eager", QExperimentType.Q_WF_NGS_ANCIENT_DNA_SEQ)
        tmpMap.put("nf-core/hlatyping", QExperimentType.Q_WF_NGS_HLATYPING)
        tmpMap.put("nf-core/mag", QExperimentType.Q_WF_NGS_SHOTGUN_METAGENOMICS_ANALYIS)
        tmpMap.put("nf-core/methylseq", QExperimentType.Q_WF_NGS_METHYLATION_ANALYSIS)
        tmpMap.put("nf-core/mhcquant", QExperimentType.Q_WF_LX_MHC_QUANTIFICATION)
        tmpMap.put("nf-core/rnaseq", QExperimentType.Q_WF_NGS_RNA_SEQ)
        tmpMap.put("nf-core/sarek", QExperimentType.Q_WF_NGS_VARIANT_CALLING)
        tmpMap.put("nf-core/scrnaseq", QExperimentType.Q_WF_NGS_SCELL_RNA_SEQ)
        tmpMap.put("nf-core/smrnaseq", QExperimentType.Q_WF_NGS_SMALL_RNA_SEQ)
        tmpMap.put("qbic-pipelines/cellranger", QExperimentType.Q_WF_NGS_SCELL_RNA_SEQ)
        tmpMap.put("qbic-pipelines/rnadeseq", QExperimentType.Q_WF_NGS_RNA_EXPRESSION_ANALYSIS)
        PIPELINE_TO_EXPERIMENT_TYPE = Collections.unmodifiableMap(tmpMap)
    }

    private static final Map<String, QSampleType> PIPELINE_TO_SAMPLE_TYPE
    static {
        Map<String, QSampleType> tmpMap = new HashMap<>()
        tmpMap.put("nf-core/atacseq", QSampleType.Q_WF_NGS_ATAC_SEQ_RUN)
        tmpMap.put("nf-core/ampliseq", QSampleType.Q_WF_NGS_16S_AMPLICON_SEQ_RUN)
        tmpMap.put("nf-core/bcellmacgic", QSampleType.Q_WF_NGS_BCELL_REPERTOIRE_SEQ_RUN)
        tmpMap.put("nf-core/chipseq", QSampleType.Q_WF_NGS_CHIP_SEQ_RUN)
        tmpMap.put("nf-core/eager", QSampleType.Q_WF_NGS_ANCIENT_DNA_SEQ_RUN)
        tmpMap.put("nf-core/hlatyping", QSampleType.Q_WF_NGS_HLATYPING_RUN)
        tmpMap.put("nf-core/mag", QSampleType.Q_WF_NGS_SHOTGUN_METAGENOMICS_RUN)
        tmpMap.put("nf-core/metaboigniter", QSampleType.Q_WF_MX_IDENTIFY_AND_QUANTIFY_RUN)
        tmpMap.put("nf-core/methylseq", QSampleType.Q_WF_NGS_METHYLATION_ANALYSIS_RUN)
        tmpMap.put("nf-core/mhcquant", QSampleType.Q_WF_LX_MHC_QUANTIFICATION_RUN)
        tmpMap.put("nf-core/rnaseq", QSampleType.Q_WF_NGS_RNA_SEQ_RUN)
        tmpMap.put("nf-core/sarek", QSampleType.Q_WF_NGS_VARIANT_CALLING_RUN)
        tmpMap.put("nf-core/scrnaseq", QSampleType.Q_WF_NGS_SCELL_RNA_SEQ_RUN)
        tmpMap.put("nf-core/smrnaseq", QSampleType.Q_WF_NGS_SMALL_RNA_SEQ_RUN)
        tmpMap.put("qbic-pipelines/cellranger", QSampleType.Q_WF_NGS_SCELL_RNA_SEQ_RUN)
        tmpMap.put("qbic-pipelines/rnadeseq", QSampleType.Q_WF_NGS_RNA_EXPRESSION_RUN)
        PIPELINE_TO_SAMPLE_TYPE = Collections.unmodifiableMap(tmpMap)
    }

    private static final Map<String, QDatasetType> WF_NAME_TO_DATASET_TYPE
    static {
        Map<String, QDatasetType> tmpMap = new HashMap<>()
        tmpMap.put("nf-core/atacseq", QDatasetType.Q_WF_NGS_ATAC_SEQ_RESULTS)
        tmpMap.put("nf-core/ampliseq", QDatasetType.Q_WF_NGS_16S_AMPLICON_SEQ_RESULTS)
        tmpMap.put("nf-core/bcellmacgic", QDatasetType.Q_WF_NGS_BCELL_REPERTOIRE_SEQ_RESULTS)
        tmpMap.put("nf-core/chipseq", QDatasetType.Q_WF_NGS_CHIP_SEQ_RESULTS)
        tmpMap.put("nf-core/eager", QDatasetType.Q_WF_NGS_ANCIENT_DNA_SEQ_RESULTS)
        tmpMap.put("nf-core/hlatyping", QDatasetType.Q_WF_NGS_HLATYPING_RESULTS)
        tmpMap.put("nf-core/mag", QDatasetType.Q_WF_NGS_SHOTGUN_METAGENOMICS_RESULTS)
        tmpMap.put("nf-core/metaboigniter", QDatasetType.Q_WF_MX_IDENTIFY_AND_QUANTIFY_RESULTS)
        tmpMap.put("nf-core/methylseq", QDatasetType.Q_WF_NGS_METHYLATION_ANALYSIS_RESULTS)
        tmpMap.put("nf-core/mhcquant", QDatasetType.Q_WF_LX_MHC_QUANTIFICATION_RESULTS)
        tmpMap.put("nf-core/rnaseq", QDatasetType.Q_WF_NGS_RNA_SEQ_RESULTS)
        tmpMap.put("nf-core/sarek", QDatasetType.Q_WF_NGS_VARIANT_CALLING_RESULTS)
        tmpMap.put("nf-core/scrnaseq", QDatasetType.Q_WF_NGS_SCELL_RNA_SEQ_RESULTS)
        tmpMap.put("nf-core/smrnaseq", QDatasetType.Q_WF_NGS_SMALL_RNA_SEQ_RESULTS)
        tmpMap.put("qbic-pipelines/cellranger", QDatasetType.Q_WF_NGS_SCELL_RNA_SEQ_RESULTS)
        tmpMap.put("qbic-pipelines/rnadeseq", QDatasetType.Q_WF_NGS_RNAEXPRESSIONANALYSIS_RESULTS)
        WF_NAME_TO_DATASET_TYPE = Collections.unmodifiableMap(tmpMap)
    }

    private final NfCorePipelineResult pipelineResult

    private Path datasetRootPath

    private Context context

    private final NfTower nfTower

    private String usedNfCorePipeline

    private String workflowRunId

    /**
     * <p>Creates an instance of a nf-core pipeline result registry, that
     * is able to register pipeline output from nf-core bioinformatic pipelines in openBIS.</p>
     * <br>
     * <p>The registration process needs to be triggered explicitly by calling
     * the {@link #executeRegistration} method.</p>
     *
     * @param pipelineResult the nf-core pipeline output
     * @since 1.0.0
     */
    NfCoreResultRegistry(NfCorePipelineResult pipelineResult) {
        this.pipelineResult = pipelineResult
        this.nfTower = new NfTower()
    }

    /**
     * {@inheritDocs}
     */
    @Override
    void executeRegistration(IDataSetRegistrationTransactionV2 transaction, Path datasetRootPath) throws RegistrationException {
        this.datasetRootPath = datasetRootPath

        def sampleIds = getInputSamples().orElseThrow({
             throw new RegistrationException("Could not determine sample codes that have been " +
                     "used for the nf-core pipeline run.")})

        this.workflowRunId = getTowerRunId().orElseThrow({
            throw new RegistrationException("Could not determine workflow run id from Tower.")})

        def analysisType = getAnalysisType(workflowRunId).orElseThrow({
            throw new RegistrationException("Could not determine analysis type for run ${runId}.")
        })

        this.usedNfCorePipeline = nfTower.getPipelineName(workflowRunId).orElseThrow({
            throw new RegistrationException("Could not determine pipeline name for run ${runId}.")
        })

        try {
            register(transaction, sampleIds, analysisType)
        } catch (RuntimeException e) {
            log.error("An exception occurred during the registration.")
            throw new RegistrationException(e.getMessage())
        }
    }

    private static Optional<Context> getContext(SampleId sampleId,
                                                ISearchService searchService) {
        SearchCriteria sc = new SearchCriteria()
        sc.addMatchClause(
                SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleId.toString())
        )
        List<ISampleImmutable> searchResult = searchService.searchForSamples(sc)
        if (!searchResult) {
            return Optional.empty()
        }
        ProjectSpace space = new ProjectSpace(searchResult[0].getSpace())
        ProjectCode code = sampleId.getProjectCode()

        Context context = new Context(projectSpace: space, projectCode: code)
        return Optional.of(context)
    }

    /*
    Does the final registration of the dataset in openBIS.
     */
    private void register(IDataSetRegistrationTransactionV2 transaction,
                          List<String> sampleIds,
                          QExperimentType analysisType) {
        // 1. Get the openBIS samples the datasets belong to
        // Will contain the openBIS samples which data served as input data for
        // the pipeline run
        List<SampleId> sampleIdList = validateSampleIds(sampleIds)

        this.context = getContext(sampleIdList[0], transaction.getSearchService()).orElseThrow({
            new RegistrationException("Could not determine context for samples ${sampleIdList}")
        })

        List<ISample> parentSamples = []
        for (SampleId sampleId : sampleIdList) {
            ISample sample = transaction.getSampleForUpdate("/${context.getProjectSpace()}/$sampleId")
            parentSamples.add(sample)
        }
        // Both lists must have the same length,
        // otherwise that means that not all samples where found in openBIS
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
        def sampleType = determineSampleTypeFrom(usedNfCorePipeline).orElseThrow({
            throw new RegistrationException("Cannot infere sample type for pipeline $usedNfCorePipeline")
        })
        def newAnalysisRunId = existingAnalysisRunIds ? existingAnalysisRunIds.last().nextId() : new AnalysisResultId(1)
        // New sample code /<space>/<project code>R<number>
        def newRunSampleId = "/${context.getProjectSpace().toString()}/${context.getProjectCode().toString()}${newAnalysisRunId.toString()}"
        def newOpenBisSample = transaction.createNewSample(newRunSampleId, sampleType.toString())

        // 5. Create new experiment
        ExperimentId newExperimentId = existingExperimentIds ? existingExperimentIds.last().nextId() : new ExperimentId(1)
        // New sample code /<space>/<project code>/<project code>E<number>
        def newExperimentFullId = "/${context.getProjectSpace().toString()}/" +
                "${context.getProjectCode().toString()}/" +
                "${context.getProjectCode().toString()}${newExperimentId.toString()}"

        def newExperiment = transaction.createNewExperiment(newExperimentFullId, analysisType.toString())
        // Set Tower run id and the used pipeline
        newExperiment.setPropertyValue(QPropertyTypes.Q_WF_ID.toString(), workflowRunId)
        newExperiment.setPropertyValue(QPropertyTypes.Q_WF_NAME.toString(), usedNfCorePipeline)

        // 6. Set parent samples as parents in the newly created run result sample
        newOpenBisSample.setParentSampleIdentifiers(sampleIds)

        // 7. Set experiment for run result sample
        newOpenBisSample.setExperiment(newExperiment)

        // 8. Create new openBIS dataset
        def dataset = transaction.createNewDataSet(WF_NAME_TO_DATASET_TYPE.get(this.usedNfCorePipeline) as String)
        dataset.setSample(newOpenBisSample)

        // 9. Attach result data to dataset
        transaction.moveFile(this.datasetRootPath.toString(), dataset)
    }

    private List<SampleId> validateSampleIds(List<String> sampleIdList) throws RuntimeException {
        def convertedSampleIds = []
        for(String sampleId : sampleIdList) {
            def convertedId = SampleId.from(sampleId).orElseThrow( {
                throw new RuntimeException("$sampleId does not seem to contain a valid sample id.")})
            convertedSampleIds.add(convertedId)
        }
        return convertedSampleIds
    }

    /*
    Returns the measurement sample codes used for the analysis as input.
     */
    private Optional<List<String>> getInputSamples() {
        def sampleIdPath = Paths.get(datasetRootPath.toString(), pipelineResult.sampleIds.relativePath)
        def sampleIds = parseSampleIdsFrom(sampleIdPath)
        sampleIds ? Optional.of(sampleIds) : Optional.empty() as Optional<List<String>>
    }

    /*
    Iterates through the lines of a file and extracts the sample codes.
    The sample codes must be line separated. All trailing whitespace will get trimmed.
     */
    private List<String> parseSampleIdsFrom(Path file) {
        def sampleIds = []
        try {
            def fileRowEntries = new File(file.toUri()).readLines()
            for (String row : fileRowEntries) {
                sampleIds.add(row.trim())
            }
        } catch (Exception e) {
            switch (e) {
                case FileNotFoundException:
                    log.error "File ${file} was not found."
                    break
                default:
                    log.error "Could not read from file ${file}."
                    log.error "Reason: ${e.stackTrace.join("\n")}"
            }
        }
        return sampleIds
    }

    /*
    Returns the unique workflow run id from Nextflow Tower.
     */
    private Optional<String> getTowerRunId() {
        def runIdPath = Paths.get(datasetRootPath.toString(), pipelineResult.runId.relativePath)
        String runId = parseRunIdFrom(runIdPath)
        return runId ? Optional.of(runId) : Optional.empty() as Optional<String>
    }

    /*
    Parse the run id from the given file.
     */
    private String parseRunIdFrom(Path file) {
        def runId = ""
        try {
            runId = new File(file.toUri()).getText()
            runId = runId.trim()
        } catch (Exception e) {
            switch (e) {
                case FileNotFoundException:
                    log.error "File ${file} was not found."
                    break
                default:
                    log.error "Could not read from file ${file}."
                    log.error "Reason: ${e.stackTrace.join("\n")}"
            }
        }
        return runId
    }

    /*
    Returns the analysis type.
     */
    private Optional<QExperimentType> getAnalysisType(String runId) {
        String pipelineName = nfTower.getPipelineName(runId).orElseThrow({
            throw new RegistrationException("Could not determine pipeline name from Nextflow Tower for run id $runId")})
        return determineAnalysisTypeFrom(pipelineName)
    }

    /*
    Returns the associated analysis type based on the pipeline name.
     */
    private Optional<QExperimentType> determineAnalysisTypeFrom(String pipelineName) {
        QExperimentType type = PIPELINE_TO_EXPERIMENT_TYPE.get(pipelineName)
        return type ? Optional.of(type) : Optional.empty() as Optional<QExperimentType>
    }

    /*
    Returns the associated sample type based on the pipeline name.
     */
    private Optional<QSampleType> determineSampleTypeFrom(String pipelineName) {
        def type = PIPELINE_TO_SAMPLE_TYPE.get(pipelineName)
        return type ? Optional.of(type) : Optional.empty() as Optional<QSampleType>
    }

}
