package life.qbic.registration.handler;

import java.nio.file.Path;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
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
