package life.qbic.registration.handler;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
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
