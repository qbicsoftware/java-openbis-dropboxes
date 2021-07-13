package life.qbic.registration.registries

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
import groovy.util.logging.Log4j2
import life.qbic.datamodel.dtos.projectmanagement.ProjectCode
import life.qbic.datamodel.dtos.projectmanagement.ProjectSpace
import life.qbic.registration.Context
import life.qbic.registration.SampleId

import java.nio.file.Path

/**
 * <p>A collection of useful methods that can be shared among the different registries.</p>
 *
 * @since 1.1.0
 */
@Log4j2
class Utils {

    /**
     * <p>Iterates through the lines of a file and extracts the sample codes.
     * The sample codes must be line separated.
     * All trailing whitespace will get trimmed.</p>
     * @param file the path to the sample id file
     * @return a list of sample ids
     * @since 1.1.0
     */
    static Optional<List<String>> parseSampleIdsFrom(Path file) {
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
        return sampleIds ? Optional.of(sampleIds) : Optional.empty() as Optional<List<String>>
    }

    /**
     * <p> Tries to determine the registration {@link Context} based on a given sample identifier.<p>
     * @param sampleId the sample id to infer the {@link Context}
     * @param searchService the openBIS search service
     * @return
     */
    static Optional<Context> getContext(SampleId sampleId,
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

    /**
     * <p>Validates and converts a list of String sample ids into a list of {@link SampleId}.</p>
     * @param sampleIdList a list of sample ids
     * @return a list of converted and validated sample ids
     * @throws RuntimeException if at least one sample id cannot be converted
     */
    static List<SampleId> validateSampleIds(List<String> sampleIdList) throws RuntimeException {
        def convertedSampleIds = []
        for(String sampleId : sampleIdList) {
            def convertedId = SampleId.from(sampleId).orElseThrow( {
                throw new RuntimeException("$sampleId does not seem to contain a valid sample id.")})
            convertedSampleIds.add(convertedId)
        }
        return convertedSampleIds
    }
}
