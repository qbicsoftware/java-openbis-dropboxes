package life.qbic.registration.handler.registries

import groovy.json.JsonSlurper
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.uri.UriBuilder

/**
 * <class short description - 1 Line!>
 *
 * <More detailed description - When to use, what it solves, etc.>
 *
 * @since <versiontag>
 */
class NfTower {

    private final String NXF_TOWER_API_URL

    private final String NXF_TOWER_API_TOKEN

    NfTower() {
        NXF_TOWER_API_URL = Objects.requireNonNull(
                System.getenv("NXF_TOWER_API_URL"),
                "The NXF_TOWER_API_URL environment variable was not set.")
        NXF_TOWER_API_TOKEN = Objects.requireNonNull(
                System.getenv("NXF_TOWER_API_TOKEN"),
        "The NXF_TOWER_API_TOKEN environment variable was not set.")
    }

    Optional<String> getPipelineName(String workflowId) {
        RxHttpClient httpClient = RxHttpClient.create(NXF_TOWER_API_URL as URL)
        String query = UriBuilder.of("${NXF_TOWER_API_URL}/workflow/{workflowId}")
                .expand(Collections.singletonMap("workflowId", workflowId))
                .toString()
        def result = httpClient.toBlocking().retrieve(query)
        def content = new JsonSlurper().parseText(result) as Map
        // This should get the Github pipeline repo slug
        // like 'nf-core/rnaseq'
        def pipelineName = (content.get("workflow") as Map).get("repository") as String

        return pipelineName ? Optional.of(pipelineName) : Optional.empty() as Optional<String>
    }
}