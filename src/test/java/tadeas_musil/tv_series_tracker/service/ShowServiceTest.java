package tadeas_musil.tv_series_tracker.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.SearchResult;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.List;

@SpringBootTest(properties = { "trakt_tv.api.uri.search_show_by_id=http://localhost:8080/test",
                               "trakt_tv.api.uri.search_shows_by_query=http://localhost:8080/test",
                               "trakt_tv.api.uri.search_get_schedule=http://localhost:8080/test",
                               "trakt_tv.api.uri.search_premiering_shows=http://localhost:8080/test",
                               "tvmaze.api.uri.search_show_by_tvdbid=http://localhost:8080/test" })
@RunWith(SpringRunner.class)
public class ShowServiceTest {
  
  @Autowired
  private ShowService showService;

  @MockBean
  private ShowRepository showRepository;
  
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(
    options().usingFilesUnderDirectory("src/test/java/tadeas_musil/tv_series_tracker/resources"));

  @Test
  public void findShow_shouldReturnCorrectShow() {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                                 .withHeader("Content-Type", "application/json")
                                                 .withBodyFile("search-show-by-id.json")));
    Show show = showService.findShow("id");
    
    assertThat(show).hasFieldOrPropertyWithValue("title", "Bugs")
                    .hasFieldOrPropertyWithValue("year", 1995)
                    .hasFieldOrPropertyWithValue("traktId", "7")
                    .hasFieldOrPropertyWithValue("imdbId", "tt0111904")
                    .hasFieldOrPropertyWithValue("tvdbId", "77698");
  }

  @Test
  public void searchShows_shouldReturnTenShows() throws Exception {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                               .withHeader("X-Pagination-Page-Count", "20")
                                               .withHeader("Content-Type", "application/json")
                                               .withBodyFile("search-shows-by-query.json")));
    
    SearchResult searchResult = showService.searchShows("query", 1);
    
    assertThat(searchResult.getTotalNumberOfPages()).isEqualTo(20);
    assertThat(searchResult.getShows()).hasSize(10)
                                       .first().hasFieldOrPropertyWithValue("title", "Planet Earth")
                                               .hasFieldOrPropertyWithValue("year", 2006)
                                               .hasFieldOrPropertyWithValue("traktId", "1039")
                                               .hasFieldOrPropertyWithValue("imdbId", "tt0795176");
  }

  @Test
  public void getSchedule_shouldReturn4Shows() {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                               .withHeader("Content-Type", "application/json")
                                               .withBodyFile("get-airing-episodes.json")));
    
    List<Episode> episodes = showService.getAiringEpisodes();
    
    assertThat(episodes).hasSize(4)
                        .first().hasFieldOrPropertyWithValue("title", "Death is Not the End")
                                .hasFieldOrPropertyWithValue("season", "7")
                                .hasFieldOrPropertyWithValue("number", "4")
                        .extracting(Episode::getShow).hasFieldOrPropertyWithValue("title", "True Blood")
                                                     .hasFieldOrPropertyWithValue("year", 2008);

  }

  @Test
  public void getImageUrl_shouldReturnCorrectUrl() throws Exception {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                                 .withHeader("Content-Type", "application/json")
                                                 .withBodyFile("get-image-url.json")));

    assertThat(showService.getImageUrl("id")).isEqualTo("http://static.tvmaze.com/uploads/images/original_untouched/67/169692.jpg");
  }

  @Test
  public void getImageUrl_shouldReturnEmptyString_whenResponseIs404() throws Exception {
    givenThat(get("/test").willReturn(aResponse().withStatus(404)));

    assertThat(showService.getImageUrl("id")).isEqualTo("");
  }

  @Test
  public void getImageUrl_shouldReturnEmptyString_whenJsonPropertyImageIsNull() throws Exception {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                                 .withHeader("Content-Type", "application/json")
                                                 .withBodyFile("image-is-null.json")));

    assertThat(showService.getImageUrl("id")).isEqualTo("");
  }

  @Test
  public void getPremieringShows_shouldReturnTwoShows_WhenOneShowHasImdbIdNullAndOneShowHasIncorrectImdbIdFormat() {
    givenThat(get("/test").willReturn(aResponse().withStatus(200)
                                               .withHeader("Content-Type", "application/json")
                                               .withBodyFile("get-premiering-shows.json")));
    
    List<Show> episodes = showService.getPremieringShows();
    
    assertThat(episodes).hasSize(2)
                        .first().hasFieldOrPropertyWithValue("title", "True Blood")
                                .hasFieldOrPropertyWithValue("year", 2008);

  }

  @Test
  public void getRecommendedShows_shouldReturnThreeShows_WhenOneShowHasImdbIdNull() {
    Show planetEarth = new Show();
    planetEarth.setTitle("planetEarth");
    Page<Show> page = new PageImpl<Show>(List.of(planetEarth));
    when(showRepository.findByIsRecommendedOrderByReleaseDateDesc(anyBoolean(), any())).thenReturn(page);
    
    Page<Show> recommendedShows = showService.getRecommendedShows(0);
    
    assertThat(recommendedShows.getContent()).hasSize(1)
                                              .first().isEqualTo(planetEarth);

  }
}