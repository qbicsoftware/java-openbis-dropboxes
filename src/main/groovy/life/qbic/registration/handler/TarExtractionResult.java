package life.qbic.registration.handler;

import java.nio.file.Path;

/**
 * <b>Tar Extraction Result</b>
 * <p>
 * Information container for successful tar extraction results
 *
 * @since 1.6.0
 */
public class TarExtractionResult {

  private final TarArchive archive;

  private final Path destination;


  public TarExtractionResult(TarArchive archive, Path destination) {
    this.archive = archive;
    this.destination = destination;
  }

  public TarArchive archive() {
    return archive;
  }

  public Path destination() {
    return destination;
  }
}
