package life.qbic.registration.handler.registries

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
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

   private enum AnalysisType {
        VARIANT_CALLING
   }

    /**
     * {@inheritDocs}
     */
    @Override
    void executeRegistration(IDataSetRegistrationTransactionV2 transaction) throws RegistrationException {

    }

    /*
    Returns the measurement sample codes used for the analysis as input.
     */
    private List<String> getInputSamples() {
        parseSampleIdsFrom(Paths.get("./"))
    }
    /*
    Iterates through the lines of a file and extracts the sample codes.
    The sample codes must be line separated. All trailing whitespace will get trimmed.
     */
    private List<String> parseSampleIdsFrom(Path file) {
        null
    }
    /*
    Returns the unique workflow run id from Nextflow Tower.
     */
    private String getTowerRunId() {
        parseRunIdFrom(Paths.get("./"))
    }
    /*
    Parse the run id from the given file.
     */
    private String parseRunIdFrom(Path file) {
        null
    }
    /*
    Returns the analysis type.
     */
    private AnalysisType getAnalysisType() {
        String pipelineName = getPipelineName()
        determineAnalysisTypeFrom(pipelineName)
    }
    /*
    Returns the pipeline name
     */
    private String getPipelineName() {
        null
    }
    /*
    Returns the associated analysis type based on the pipeline name.
     */
    private AnalysisType determineAnalysisTypeFrom(String pipelineName) {
        null
    }

}
