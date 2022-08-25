package life.qbic.registration.handler;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class TarArchive {

  private final Path path;

  public TarArchive(Path path) throws IllegalArgumentException {
    requireNonNull(path, "Path must not be null");
    if (!path.getFileName().toString().endsWith(".tar")) {
      throw new IllegalArgumentException(path + " seems not to be a TAR archive");
    }
    this.path = path;
  }

  public Path path() {
    return path;
  }

  public String name() {
    return path.getFileName().toString().replace(".tar$", "");
  }
}
