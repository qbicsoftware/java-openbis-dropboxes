package life.qbic.registration.handler.registries

/**
 * <p>Enables interaction with a backend workflow monitor system.</p>
 *
 * @since 1.0.0
 */
interface WorkflowMonitor {

    /**
     * Returns the pipeline repo slug for a given workflow identifier.
     * @param workflowId the unique identifier of the workflow
     * @return A pipeline repo slug, if found.
     */
    Optional<String> getPipelineName(String workflowId)

}