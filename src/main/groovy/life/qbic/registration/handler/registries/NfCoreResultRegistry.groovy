package life.qbic.registration.handler.registries

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.SearchService
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
import groovy.util.logging.Log4j2
import life.qbic.datamodel.datasets.NfCorePipelineResult
import life.qbic.datamodel.dtos.projectmanagement.ProjectCode
import life.qbic.datamodel.dtos.projectmanagement.ProjectIdentifier
import life.qbic.datamodel.dtos.projectmanagement.ProjectSpace
import life.qbic.registration.AnalysisResultId
import life.qbic.registration.Context
import life.qbic.registration.ExperimentId
import life.qbic.registration.SampleId
import life.qbic.registration.handler.RegistrationException
import life.qbic.registration.handler.Registry

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

    private static enum AnalysisType {
        RNA_SEQ,
        VARIANT_CALLING
    }

    private static enum SampleType {
        ANALYSIS_WORKFLOW_RESULT
    }

    private static enum DataSetType {
        NF_CORE_RESULT
    }

    private static final Map<String, AnalysisType> PIPELINE_TO_ANALYSIS
    static {
        Map<String, AnalysisType> tmpMap = new HashMap<>()
        tmpMap.put("nf-core/rnaseq", AnalysisType.RNA_SEQ)
        tmpMap.put("nf-core/sarek", AnalysisType.VARIANT_CALLING)
        PIPELINE_TO_ANALYSIS = Collections.unmodifiableMap(tmpMap)
    }

    private final NfCorePipelineResult pipelineResult

    private Path datasetRootPath

    private Context context

    private final NfTower nfTower

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
        log.info "Los gehts"
        def sampleIds = getInputSamples().orElseThrow({
             throw new RegistrationException("Could not determine sample codes that have been " +
                     "used for the nf-core pipeline run.")})

        def runId = getTowerRunId().orElseThrow({
            throw new RegistrationException("Could not determine workflow run id from Tower.")})

        def analysisType = getAnalysisType(runId).orElseThrow({
            throw new RegistrationException("Could not determine analysis type for run ${runId}.")
        })

        log.info "Los gehts 2"

        try {
            register(transaction, sampleIds, analysisType)
        } catch (RuntimeException e) {
            new RegistrationException(e.message)
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
                          AnalysisType analysisType) {
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
        log.info sampleIdList
        log.info parentSamples
        log.info "3"
        log.info context

        // 2. Get existing analysis run results
        ISearchService searchService = transaction.getSearchService()
        SearchCriteria searchCriteriaResultSamples = new SearchCriteria()

        searchCriteriaResultSamples.addMatchClause(
                SearchCriteria.MatchClause.createAnyFieldMatch("${sampleIdList[0].getProjectCode().toString()}R")
        )

        List<ISampleImmutable> existingAnalysisResultSamples = searchService.searchForSamples(searchCriteriaResultSamples)
        log.info existingAnalysisResultSamples
        List<AnalysisResultId> existingAnalysisRunIds = []
        for (ISampleImmutable sample in existingAnalysisResultSamples) {
            AnalysisResultId id = AnalysisResultId.parseFrom(sample.code)
            existingAnalysisRunIds.add(id)
        }
        existingAnalysisRunIds = existingAnalysisRunIds.sort()

        // 3. Get existing experiments
        List<IExperimentImmutable> existingExperiments = searchService.listExperiments(sampleIdList[0].getProjectCode().toString()) as List<IExperimentImmutable>
        log.info existingExperiments
        List<ExperimentId> existingExperimentIds = []
        for (IExperimentImmutable experiment in existingExperiments) {
            ExperimentId id = ExperimentId.parseFrom(experiment.experimentIdentifier)
            existingExperimentIds.add(id)
        }
        existingExperimentIds = existingExperimentIds.sort()

        // 4. Create new run result sample
        def newAnalysisRunId = existingAnalysisRunIds ? existingAnalysisRunIds.last().nextId() : new AnalysisResultId(1)
        // New sample code /<space>/<project code>R<number>
        def newRunSampleId = "/${context.projectSpace.toString()}/${context.projectCode.toString()}${newAnalysisRunId.toString()}"
        log.info newRunSampleId
        def newOpenBisSample = transaction.createNewSample(newRunSampleId, SampleType.ANALYSIS_WORKFLOW_RESULT.toString())

        // 5. Create new experiment
        ExperimentId newExperimentId = existingExperimentIds ? existingExperimentIds.last().nextId() : new ExperimentId(1)
        // New sample code /<space>/<project code>E<number>
        def newExperimentFullId = "/${context.projectSpace.toString()}/${context.projectCode.toString()}${newExperimentId.toString()}"
        log.info newExperimentFullId
        def newExperiment = transaction.createNewExperiment(newExperimentFullId, analysisType.toString())

        // 6. Set parent samples as parents in the newly created run result sample
        newOpenBisSample.setParentSampleIdentifiers(sampleIds)

        // 7. Set experiment for run result sample
        newOpenBisSample.setExperiment(newExperiment)

        // 8. Create new openBIS dataset
        def dataset = transaction.createNewDataSet(DataSetType.NF_CORE_RESULT.toString())
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
        println sampleIdPath
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
                    println "File ${file} was not found."
                    break
                default:
                    println "Could not read from file ${file}."
                    println "Reason: ${e.stackTrace.join("\n")}"
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
                    println "File ${file} was not found."
                    break
                default:
                    println "Could not read from file ${file}."
                    println "Reason: ${e.stackTrace.join("\n")}"
            }
        }
        return runId
    }

    /*
    Returns the analysis type.
     */
    private Optional<AnalysisType> getAnalysisType(String runId) {
        String pipelineName = nfTower.getPipelineName(runId).orElseThrow({
            throw new RegistrationException("Could not determine pipeline name from Nextflow Tower for run id $runId")})
        return determineAnalysisTypeFrom(pipelineName)
    }

    /*
    Returns the associated analysis type based on the pipeline name.
     */
    private Optional<AnalysisType> determineAnalysisTypeFrom(String pipelineName) {
        AnalysisType type = PIPELINE_TO_ANALYSIS.get(pipelineName)
        return type ? Optional.of(type) : Optional.empty() as Optional<AnalysisType>
    }

}
