package life.qbic.registration.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * <b>Tar Archive Handler</b>
 * <p>
 * Manages extraction of tar archives.
 * <p>
 * The handler utilises the UNIX command line tool <code>tar</code> to perform the actual extraction
 * of the archive.
 * <p>
 * The handler creates a new sub-process for every execution of the public extraction methods and
 * waiting for the process to exit. For error handling, the exit codes are used to determine, if the
 * extraction was successful or not.
 * <p>
 * Every exit code value different from <code>0</code> will be regarded as failure and the error output stream of
 * the process will be parsed and provided in the returned {@link TarExtractionFailure} object.
 * <p>
 * Find more information about the command line tool on the gnu.org homepage:
 * <a href="https://www.gnu.org/software/tar/">https://www.gnu.org/software/tar/</a>
 *
 * @since 1.6.0
 */
public class TarArchiveHandler {

  private static final String TAR_COMMAND = "tar -xf %s -C %s";

  /**
   * Extracts a tar archive's content to a given destination.
   *
   * @param archive     the tar archive to extract
   * @param destination the destination for the extracted content. This directory must exist and
   *                    provided with write access
   * @param onSuccess   callback function that is executed when the extraction was successful
   * @param onError     callback function that is executed when the extraction failed
   * @since 1.6.0
   */
  public static void extract(TarArchive archive, Path destination,
      Consumer<TarExtractionResult> onSuccess,
      Consumer<TarExtractionFailure> onError) {
    triggerExtractionTo(archive, destination, onSuccess, onError);
  }

  /**
   * Extracts a tar archive's content to the parent directory of the archive.
   *
   * @param archive   the tar archive to extract
   * @param onSuccess callback function that is executed when the extraction was successful
   * @param onError   callback function that is executed when the extraction failed
   * @since 1.6.0
   */
  public static void extract(TarArchive archive, Consumer<TarExtractionResult> onSuccess,
      Consumer<TarExtractionFailure> onError) {
    extract(archive, archive.path().getParent(), onSuccess, onError);
  }

  private static void triggerExtractionTo(TarArchive archive, Path destination,
      Consumer<TarExtractionResult> onSuccess,
      Consumer<TarExtractionFailure> onError) {
    try {
      Process process = Runtime.getRuntime()
          .exec(String.format(TAR_COMMAND, archive.path().toString(), destination.toString()));
      process.waitFor();
      if (process.exitValue() == 0) {
        reportSuccessfulExtraction(archive, destination, onSuccess);
      } else {
        reportExtractionFailure(archive, process.getErrorStream(), onError);
      }
    } catch (IOException e) {
      TarExtractionFailure failure = new TarExtractionFailure(archive, "IOException occurred",
          e.getMessage());
      onError.accept(failure);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static void reportExtractionFailure(TarArchive archive, InputStream errorStream,
      Consumer<TarExtractionFailure> onFailure) {
    String errorInfo = readInputStream(errorStream);
    TarExtractionFailure failure = new TarExtractionFailure(archive, "Failed", errorInfo);
    onFailure.accept(failure);
  }

  private static String readInputStream(InputStream inputStream) {
    StringBuilder builder = new StringBuilder();
    try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      int c;
      while ((c = reader.read()) != -1) {
        builder.append((char) c);
      }
    } catch (IOException e) {
      builder.append(e.getMessage());
    }
    return builder.toString();
  }

  private static void reportSuccessfulExtraction(TarArchive archive, Path destination,
      Consumer<TarExtractionResult> onSuccess) {
    TarExtractionResult result = new TarExtractionResult(archive, destination);
    onSuccess.accept(result);
  }


}
