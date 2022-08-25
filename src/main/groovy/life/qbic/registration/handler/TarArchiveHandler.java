package life.qbic.registration.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class TarArchiveHandler {

  private static final String TAR_COMMAND = "tar -xf %s -C %s";

  public static void extractTo(TarArchive archive, Path destination, Consumer<TarExtractionResult> onSuccess,
      Consumer<TarExtractionFailure> onError) {
    triggerExtractionTo(archive, destination, onSuccess, onError);
  }
  public static void extract(TarArchive archive, Consumer<TarExtractionResult> onSuccess,
      Consumer<TarExtractionFailure> onError) {
    extractTo(archive, archive.path().getParent(), onSuccess, onError);
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

  private static void reportExtractionFailure(TarArchive archive, InputStream errorStream, Consumer<TarExtractionFailure> onFailure) {
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
