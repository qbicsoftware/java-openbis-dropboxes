package life.qbic.registration.handler.registries

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample
import groovy.json.JsonSlurper
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.uri.UriBuilder
import life.qbic.datamodel.datasets.NfCorePipelineResult
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
 *
 * @since 1.0.0
 */
class NfCoreResultRegistry implements Registry {

    // TODO: Hardcoded for know, we need some nicer way to ingest these properties (i.e. via beans)
    private static final String NEXTFLOW_API_URL =
            "http://cfgateway1.zdv.uni-tuebingen.de/api/"

    private enum AnalysisType {
        RNA_SEQ,
        VARIANT_CALLING
    }

    private static final Map<String, AnalysisType> PIPELINE_TO_ANALYSIS
    static {
        PIPELINE_TO_ANALYSIS.put("nf-core/rnaseq", AnalysisType.RNA_SEQ)
    }

    private final NfCorePipelineResult pipelineResult

    private Path datasetRootPath

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
    }

    /**
     * {@inheritDocs}
     */
    @Override
    void executeRegistration(IDataSetRegistrationTransactionV2 transaction) throws RegistrationException {
        datasetRootPath = transaction.incoming.toPath()
        def sampleIds = getInputSamples().orElseThrow({
             throw new RegistrationException("Could not determine sample codes that have been " +
                     "used for the nf-core pipeline run.")})

        def runId = getTowerRunId().orElseThrow({
            throw new RegistrationException("Could not determine workflow run id from Tower.")})

        def analysisType = getAnalysisType(runId).orElseThrow({
            throw new RegistrationException("Could not determine analysis type for run ${runId}.")
        })

        register(transaction, sampleIds, analysisType)
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
        List<ISample> parentSamples = []
        for (String sampleId : sampleIds) {
            ISample sample = transaction.getSampleForUpdate(sampleId)
            parentSamples.add(sample)
        }

        // 2. Get existing analysis run results

        // 3. Get existing experiments

        // 4. Create new run result sample

        // 5. Create new experiment

        // 6. Set parent samples as parents in the newly created run result sample

        // 7. Set experiment for run result sample

        // 8. Create new openBIS dataset

        // 9. Attach result data to dataset

        // Finish transaction
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
        String pipelineName = getPipelineName(runId)
        return determineAnalysisTypeFrom(pipelineName)
    }

    /*
    Returns the pipeline name based on the Nextflow Tower workflow id
     */
    private String getPipelineName(String workflowId) {
        RxHttpClient httpClient = RxHttpClient.create(NEXTFLOW_API_URL as URL)
        String query = UriBuilder.of("${NEXTFLOW_API_URL}/workflow/{workflowId}")
                .expand(Collections.singletonMap("workflowId", workflowId))
                .toString()
        def result = httpClient.toBlocking().retrieve(query)
        def content = new JsonSlurper().parseText(result) as Map
        // This should get the Github pipeline repo slug
        // like 'nf-core/rnaseq'
        return (content.get("workflow") as Map).get("repository") as String
    }

    /*
    Returns the associated analysis type based on the pipeline name.
     */
    private Optional<AnalysisType> determineAnalysisTypeFrom(String pipelineName) {
        AnalysisType type = PIPELINE_TO_ANALYSIS.get(pipelineName)
        return type ? Optional.of(type) : Optional.empty() as Optional<AnalysisType>
    }

}
