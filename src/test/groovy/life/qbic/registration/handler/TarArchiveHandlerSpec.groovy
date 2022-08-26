package life.qbic.registration.handler

import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Consumer

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
class TarArchiveHandlerSpec extends Specification {

    static Path VALID_TAR_ARCHIVE = Paths.get(getClass().getResource("/validArchive.tar").toURI())

    def "Given a valid tar archive, extract the archive and call the onSuccess consumer"() {
        given:
        TarArchive archive = new TarArchive(VALID_TAR_ARCHIVE)

        and:
        Boolean onSuccessCalled = false
        TarExtractionResult result

        when:
        TarArchiveHandler.extract(archive, new Consumer<TarExtractionResult>() {
            @Override
            void accept(TarExtractionResult tarExtractionResult) {
                result = tarExtractionResult
                onSuccessCalled = true
            }
        }, () -> {})

        then:
        assert onSuccessCalled == true
        // Extraction worked and generated the packaged directory
        assert new File(result.destination().toString() + "/archive-content").exists()
        assert new File(result.destination().toString() + "/archive-content").listFiles().size() == 2
    }

    def "Given a failure during extraction, the onError consumer is called"() {
        given:
        TarArchive archive = new TarArchive(VALID_TAR_ARCHIVE)

        and:
        Boolean onErrorCalled = false
        TarExtractionFailure result

        when:
        TarArchiveHandler.extract(archive, Paths.get("Does/not/exist"),  () -> {}, new Consumer<TarExtractionFailure>() {
            @Override
            void accept(TarExtractionFailure tarExtractionFailure) {
                result = tarExtractionFailure
                onErrorCalled = true
            }})

        then:
        assert onErrorCalled == true
        assert result.description()
        assert result.summary()
        println result.description()
        println result.summary()
    }


}
