package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import tadeas_musil.tv_series_tracker.api.TraktTvApi;
import tadeas_musil.tv_series_tracker.api.TvMazeApi;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.model.ShowsPage;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.util.DateUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class ShowServiceTest {
  
  @InjectMocks
  private ShowService showService;

  @Mock
  private ShowRepository showRepository;

  @Mock
  private TraktTvApi traktTvApi;

  @Mock
  private ShowRatingService showRatingService;

  @Mock
  private TvMazeApi tvMazeApi;

  @Before
  public void setUp(){
    initMocks(this);
    PowerMockito.mockStatic(DateUtils.class);
  }

  @Test
  public void searchShows_shouldReturnShowWithImage_givenOneShow() throws Exception {
    Show planetEarth = new Show();
    planetEarth.setTitle("Planet Earth");
    planetEarth.setTvdbId("tvdbId");
    ShowsPage page = new ShowsPage();
    page.setTotalNumberOfPages(10);
    page.setShows(List.of(planetEarth));

    when(traktTvApi.searchShowsByQuery(anyString(), anyInt())).thenReturn(page);
    when(tvMazeApi.getImageUrl(anyString())).thenReturn("www.image.com");
    
    ShowsPage shows = showService.searchShows("query", 1);
    
    assertThat(shows.getTotalNumberOfPages()).isEqualTo(10);
    assertThat(shows.getShows()).hasSize(1)
                                       .first().hasFieldOrPropertyWithValue("title", "Planet Earth")
                                               .hasFieldOrPropertyWithValue("imageUrl", "www.image.com");
  }

  @Test
  public void getPremieringShows_shouldReturnZeroShows_GivenShowWithNullImdbId() {
    Show planetEarth = new Show();
    when(traktTvApi.getPremieringShows()).thenReturn(Lists.newArrayList(planetEarth));
    
    List<Show> shows = showService.getPremieringShows();
    
    assertThat(shows).hasSize(0);
  }

  @Test
  public void getPremieringShows_shouldReturnZeroShows_GivenShowWithInvalidImdbIdFormat() {
    Show planetEarth = new Show();
    planetEarth.setImdbId("123");
    when(traktTvApi.getPremieringShows()).thenReturn(Lists.newArrayList(planetEarth));
    
    List<Show> shows = showService.getPremieringShows();
    
    assertThat(shows).hasSize(0);
  }

  @Test
  public void getPremieringShows_shouldReturnShowWithNonNullReleaseDate_GivenShowWithCorrectImdbIdFormat() {
    Show planetEarth = new Show();
    planetEarth.setImdbId("tt123");
    when(traktTvApi.getPremieringShows()).thenReturn(Lists.newArrayList(planetEarth));
    
    PowerMockito.mockStatic(DateUtils.class);
    when(DateUtils.getCurrentDate()).thenReturn(LocalDate.now());
    
    List<Show> shows = showService.getPremieringShows();
    
    assertThat(shows).hasSize(1);
    assertThat(shows.get(0).getReleaseDate()).isNotNull();
  }

  @Test
  public void getRecommendedShows_shouldReturnOneShow_GivenOneShow() {
    Show planetEarth = new Show();
    planetEarth.setTitle("planetEarth");
    Page<Show> page = new PageImpl<Show>(List.of(planetEarth));
    when(showRepository.findByIsRecommendedOrderByReleaseDateDesc(anyBoolean(), any())).thenReturn(page);
    ReflectionTestUtils.setField(showService, "showsPerPage", 12);
    
    Page<Show> recommendedShows = showService.getRecommendedShows(0);
    
    assertThat(recommendedShows.getContent()).hasSize(1)
                                              .first().isEqualTo(planetEarth);
  }

  @Test
  public void checkRatingOfNewShow_shouldReturnFalseAndSaveShowForFutureChecks_givenShowThatDoesNotMeetAnyRequirements() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();

      when(showRatingService.getShowRating(any(), anyString())).thenReturn(showRating);
      
      boolean isRecommended = showService.checkRatingOfNewShow(show, ratings);

      assertFalse(isRecommended);
      assertTrue(show.shouldGetRatingChecked());
      verify(showRepository).save(show);
      verifyNoMoreInteractions(showRepository);
  }

  @Test
  public void checkRatingOfNewShow_shouldReturnTrueAndSaveShow_givenShowThatMeetsRatingAndVotesRequirements() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();
      
      when(showRatingService.meetsRequiredRatingAndVotes(any())).thenReturn(true);

      boolean isRecommended = showService.checkRatingOfNewShow(show, ratings);

      assertTrue(isRecommended);
      assertFalse(show.shouldGetRatingChecked());
      verify(showRepository).save(show);
      verifyNoMoreInteractions(showRepository);
  }

  @Test
  public void checkRatingOfNewShow_shouldDoNothing_givenShowThatMeetsOnlyVotesRequirement() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();
      
      when(showRatingService.meetsRequiredVotes(any())).thenReturn(true);
      
      boolean isRecommended = showService.checkRatingOfNewShow(show, ratings);

      assertFalse(isRecommended);
      assertFalse(show.shouldGetRatingChecked());
      verifyZeroInteractions(showRepository);
  }

  @Test
  public void checkRatingOfExistingShow_shouldReturnFalseAndDoNothing_givenShowThatMeetsNoRequirementsAndIsNotOld() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();
      
      when(DateUtils.getAge(any())).thenReturn(Period.ofDays(1));
      ReflectionTestUtils.setField(showService, "showMaxAge", 30);
      
      boolean isRecommended = showService.checkRatingOfExistingShow(show, ratings);

      assertFalse(isRecommended);
      verifyZeroInteractions(showRepository);
  }

  @Test
  public void checkRatingOfExistingShow_shouldReturnFalseAndStopFutureChecks_givenShowThatMeetsNoRequirementsAndIsTooOld() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();

      when(DateUtils.getAge(any())).thenReturn(Period.ofDays(1000));
      ReflectionTestUtils.setField(showService, "showMaxAge", 30);
      
      boolean isRecommended = showService.checkRatingOfExistingShow(show, ratings);

      assertFalse(isRecommended);
      verify(showRepository).setShouldGetRatingChecked(false, show.getTraktId());
      verifyNoMoreInteractions(showRepository);
  }

  @Test
  public void checkRatingOfExistingShow_shouldReturnTrueAndStopFutureChecks_givenShowThatMeetsRatingAndVotesRequirements() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();
      
      when(DateUtils.getAge(any())).thenReturn(Period.ofDays(1));
      ReflectionTestUtils.setField(showService, "showMaxAge", 30);
      when(showRatingService.meetsRequiredRatingAndVotes(any())).thenReturn(true);

      boolean isRecommended = showService.checkRatingOfExistingShow(show, ratings);

      assertTrue(isRecommended);
      verify(showRepository).setIsRecommended(true, show.getTraktId());
      verify(showRepository).setShouldGetRatingChecked(false, show.getTraktId());
      verifyNoMoreInteractions(showRepository);
  }

  @Test
  public void checkRatingOfExistingShow_shouldReturnFalseAndStopFutureChecks_givenShowThatMeetsOnlyVotesRequirementAndIsNotOld() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();
      
      when(DateUtils.getAge(any())).thenReturn(Period.ofDays(1));
      ReflectionTestUtils.setField(showService, "showMaxAge", 30);
      when(showRatingService.meetsRequiredVotes(any())).thenReturn(true);
      
      boolean isRecommended = showService.checkRatingOfExistingShow(show, ratings);

      assertFalse(isRecommended);
      verify(showRepository).setShouldGetRatingChecked(false, show.getTraktId());
      verifyNoMoreInteractions(showRepository);
  }

  @Test
  public void findRecommendedShows_shouldRecommendOneShow_givenShowThatMeetsRequirements() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();
      show.setTraktId("traktId");
      List<Show> shows = List.of(show);
      
      when(showRatingService.meetsRequiredRatingAndVotes(any())).thenReturn(true);
      
      Set<Show> recommendedShows = showService.findRecommendedShows(shows, ratings);

      assertThat(recommendedShows).hasSize(1);
  }

  @Test
  public void findRecommendedShows_shouldReturnEmptySet_givenShowThatDoesNotMeetRequirements() {
      ShowRating showRating = new ShowRating();
      List<ShowRating> ratings = List.of(showRating);
      Show show = new Show();
      show.setTraktId("traktId");
      List<Show> shows = List.of(show);
      
      when(showRatingService.meetsRequiredRatingAndVotes(any())).thenReturn(false);

      Set<Show> recommendedShows = showService.findRecommendedShows(shows, ratings);

      assertThat(recommendedShows).isEmpty();
  }
}