package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import tadeas_musil.tv_series_tracker.model.ShowRating;

public class FileServiceTest {

  private FileService fileService = new FileService();

  @AfterClass
  public static void tearDown() {
    File file = new File("decompressed.tsv");
    file.delete();
  }

  @Test
  public void parseTsv_shouldReturnCorrectShows() {
    String tsvTestFile = "src/test/java/tadeas_musil/tv_series_tracker/resources/test.tsv";

    List<ShowRating> shows = fileService.parseTsv(tsvTestFile);

    assertThat(shows).hasSize(10)
                      .first().hasFieldOrPropertyWithValue("imdbId", "tt0000001")
                              .hasFieldOrPropertyWithValue("rating", 5.6)
                              .hasFieldOrPropertyWithValue("numberOfVotes", 1528);
  }

  @Test
  public void decompressGzip_ShouldReturnBiggerFile() {
    String CompressedFilePath = "src/test/java/tadeas_musil/tv_series_tracker/resources/test.tsv.gz";
    File compressedFile = new File(CompressedFilePath);

    fileService.decompressGzip(CompressedFilePath, "decompressed.tsv");
    File decompressedFile = new File("decompressed.tsv");

    assertThat(decompressedFile.length()).isGreaterThan(compressedFile.length());
  }

}