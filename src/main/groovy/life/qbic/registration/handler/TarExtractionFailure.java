package life.qbic.registration.handler;

/**
 * <b>Tar Extraction Failure</b>
 * <p>
 * Information container for tar extraction failure reporting
 *
 * @since 1.5.0
 */
public class TarExtractionFailure {

  private final TarArchive archive;

  private final String summary;

  private final String description;

  public TarExtractionFailure(TarArchive archive, String summary, String description) {
    this.archive = archive;
    this.summary = summary;
    this.description = description;
  }

  public TarArchive archive() {
    return archive;
  }

  public String summary() {
    return summary;
  }

  public String description() {
    return description;
  }
}
