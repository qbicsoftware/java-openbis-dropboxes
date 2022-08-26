package life.qbic.registration.handler;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

/**
 * <b>Tar Archive</b>
 * <p>
 * A tape-archive like package structure of file content.
 *
 * @since 1.6.0
 */
public class TarArchive {

  private final Path path;

  /**
   * Creates an instance of a {@link TarArchive} object.
   *
   * @param path the path of the tar archive file
   * @throws IllegalArgumentException when the file path does not link to a tar archive
   * @since 1.6.0
   */
  public TarArchive(Path path) throws IllegalArgumentException {
    requireNonNull(path, "Path must not be null");
    if (!path.getFileName().toString().endsWith(".tar")) {
      throw new IllegalArgumentException(path + " seems not to be a TAR archive");
    }
    this.path = path;
  }

  /**
   * Queries the path of the tar archive file
   *
   * @return the tar archive file path
   * @since 1.6.0
   */
  public Path path() {
    return path;
  }

  /**
   * Queries the name of the tar archive file
   *
   * @return the tar archive file name (excluding the file type suffix)
   * @since 1.6.0
   */
  public String name() {
    return path.getFileName().toString().replace(".tar$", "");
  }
}
