package life.qbic.registration.handler

/**
 * <p>Dataset registration exception.</p>
 * <br>
 * <p>This exception indicates an issue during registration of datasets in openBIS.</p>
 *
 * @since 1.0.0
 */
class RegistrationException extends RuntimeException {

    /**
     * Default constructor.
     */
    RegistrationException() {
        super()
    }

    /**
     * Exception with information with an exception reason.
     * @param message some information what the exception is about.
     */
    RegistrationException(String message) {
        super(message)
    }

    /**
     * Exception with information with an exception reason and additional reference to the
     * cause, which can be another {@link Error} or {@link Exception}.
     * @param message some information what the exception is about.
     * @param cause a lower level exception that is meant to be passed to the client.
     */
    RegistrationException(String message, Throwable cause) {
        super(message, cause)
    }

}
