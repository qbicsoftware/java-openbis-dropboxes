package life.qbic.registration.handler.registries

import groovy.json.JsonSlurper
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.uri.UriBuilder

/**
 * <p>Enables basic interaction with a Nextflow Tower API.</p>
 *
 * @since 1.0.0
 */
class NfTower implements WorkflowMonitor {

    private final String NXF_TOWER_API_URL

    private final String NXF_TOWER_API_TOKEN

    private final String NXF_WORKSPACE_ID

    /**
     * Requires the environment variables NXF_TOWER_API_URL, NXF_TOWER_API_TOKEN
     * and NXF_WORKSPACE_ID to be set.
     */
    NfTower() {
        NXF_TOWER_API_URL = Objects.requireNonNull(
                System.getenv("NXF_TOWER_API_URL"),
                "The NXF_TOWER_API_URL environment variable was not set.")
        NXF_TOWER_API_TOKEN = Objects.requireNonNull(
                System.getenv("NXF_TOWER_API_TOKEN"),
        "The NXF_TOWER_API_TOKEN environment variable was not set.")
        NXF_WORKSPACE_ID = Objects.requireNonNull(
                System.getenv("NXF_WORKSPACE_ID"),
                "The NXF_WORKSPACE_ID environment variable was not set."
        )
    }

    /**
     * @inheritDocs
     */
    Optional<String> getPipelineName(String workflowId) {
        RxHttpClient httpClient = RxHttpClient.create(NXF_TOWER_API_URL as URL)
        String query = UriBuilder.of("${NXF_TOWER_API_URL}/workflow/{workflowId}")
                .expand(Collections.singletonMap("workflowId", workflowId))
                .toString()
        HttpRequest request = HttpRequest.GET(query)
        request.header("Authorization", "Bearer ${NXF_TOWER_API_TOKEN}")

        def result = httpClient.toBlocking().retrieve(request)
        def content = new JsonSlurper().parseText(result) as Map
        // This should get the Github pipeline repo slug
        // like 'nf-core/rnaseq'
        def pipelineName = ((content.get("workflow") as Map).get("manifest") as Map).get("name") as String

        return pipelineName ? Optional.of(pipelineName) : Optional.empty() as Optional<String>
    }
}
