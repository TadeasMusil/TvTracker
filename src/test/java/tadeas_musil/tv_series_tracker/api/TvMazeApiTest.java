package tadeas_musil.tv_series_tracker.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;


import tadeas_musil.tv_series_tracker.repository.ShowRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest(properties = {"tvmaze.api.uri.search_show_by_tvdbid=http://localhost:8080/test"})
@RunWith(SpringRunner.class)
public class TvMazeApiTest {
  
  @Autowired
  private TvMazeApi tvMazeApi;

  @MockBean
  private ShowRepository showRepository;
  
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(
    options().usingFilesUnderDirectory("src/test/java/tadeas_musil/tv_series_tracker/resources"));

  @Test
  public void getImageUrl_shouldReturnCorrectUrl() throws Exception {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                                  .withHeader("Content-Type", "application/json")
                                                  .withBodyFile("get-image-url.json")));

    assertThat(tvMazeApi.getImageUrl("id")).isEqualTo("http://static.tvmaze.com/uploads/images/original_untouched/67/169692.jpg");
  }

  @Test
  public void getImageUrl_shouldReturnEmptyString_whenResponseIs404() throws Exception {
    givenThat(get("/test").willReturn(aResponse().withStatus(404)));

    assertThat(tvMazeApi.getImageUrl("id")).isEqualTo("");
  }

  @Test
  public void getImageUrl_shouldReturnEmptyString_whenJsonPropertyImageIsNull() throws Exception {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                                  .withHeader("Content-Type", "application/json")
                                                  .withBodyFile("image-is-null.json")));

    assertThat(tvMazeApi.getImageUrl("id")).isEqualTo("");
  }

}