package life.qbic.registration.handler

/**
 * Determines the correct registry for a given dataset type.
 *
 * @since 1.0.0
 */
class RegistrationHandler {

    static Optional<Registry> getRegistryFor(Object dataset) {
        switch (dataset) {
            case NfCorePipelineResult:
                break
            default:
                Optional.empty()
        }
    }
}
