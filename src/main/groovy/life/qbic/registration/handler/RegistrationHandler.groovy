package life.qbic.registration.handler

import life.qbic.datamodel.datasets.MaxQuantRunResult
import life.qbic.datamodel.datasets.NfCorePipelineResult
import life.qbic.registration.registries.MaxQuantResultRegistry
import life.qbic.registration.registries.NfCoreResultRegistry

/**
 * Determines the correct registry for a given dataset type.
 *
 * @since 1.0.0
 */
class RegistrationHandler {

    /**
     * <p>Assigns the correct registry for the passed object type.</p>
     *
     * <p>The {@link Registry} can be then used to perform the dataset
     * registration.</p>
     *
     * <p>Currently supported dataset types:</p>
     * <ul>
     *     <li>{@link NfCoreResultRegistry}</li>
     *     <li>{@link MaxQuantResultRegistry}</li>
     * </ul>
     *
     * @param dataset the actual dataset that needs to be registered.
     * @return the affiliated registry or empty, if no matching registry is available.
     */
    static Optional<Registry> getRegistryFor(Object dataset) {
        Optional<Registry> registry
        switch (dataset) {
            case NfCorePipelineResult:
                registry = Optional.of(new NfCoreResultRegistry(dataset as NfCorePipelineResult))
                break
            case MaxQuantRunResult:
                registry = Optional.of(new MaxQuantResultRegistry(dataset as MaxQuantRunResult))
                break
            default:
                registry = Optional.empty()
        }
        return registry
    }
}
