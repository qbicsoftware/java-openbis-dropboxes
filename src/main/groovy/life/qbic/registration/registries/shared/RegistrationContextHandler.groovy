package life.qbic.registration.registries.shared

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
import life.qbic.datamodel.dtos.projectmanagement.ProjectCode
import life.qbic.datamodel.dtos.projectmanagement.ProjectSpace
import life.qbic.registration.Context
import life.qbic.registration.SampleId

/**
 * <p>Provides access to the registration context<p>
 *
 * @since 1.0.0
 */
class RegistrationContextHandler {
    /**
     * <p> Tries to determine the registration {@link life.qbic.registration.Context} based on a given sample identifier.<p>
     * @param sampleId the sample id to infer the {@link life.qbic.registration.Context}
     * @param searchService the openBIS search service
     * @return the current registration context
     * @since 1.1.0
     */
    static Optional<Context> getContext(SampleId sampleId,
                                        ISearchService searchService) {
        SearchCriteria searchCriteria = new SearchCriteria()
        searchCriteria.addMatchClause(
                SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleId.toString())
        )
        List<ISampleImmutable> searchResult = searchService.searchForSamples(searchCriteria)
        if (!searchResult) {
            return Optional.empty()
        }
        ProjectSpace space = new ProjectSpace(searchResult[0].getSpace())
        ProjectCode code = sampleId.getProjectCode()

        Context context = new Context(projectSpace: space, projectCode: code)
        return Optional.of(context)
    }
}
