package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import tadeas_musil.tv_series_tracker.config_properties.ImdbRatingsProperties;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.util.DateUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class ShowRatingServiceTest {

    private ShowRatingService showRatingService;

    @Mock
    private ImdbRatingsProperties imdbProperties;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowService showService;

    @Before
    public void setUp() {
        showRatingService = new ShowRatingService(imdbProperties, showRepository, showService);
        PowerMockito.mockStatic(DateUtils.class);
        when(imdbProperties.getMaxAge()).thenReturn(30);
        when(imdbProperties.getRequiredRating()).thenReturn((float) 8.0);
        when(imdbProperties.getRequiredVotes()).thenReturn(1000);
    }

    @Test
    public void stopFutureChecksIfOldOrRecommended_shouldNotDoAnything_whenGivenNewShow() {
        Show show = new Show();
        show.setShouldGetRatingChecked(true);
        when(DateUtils.getAge(show.getCreationDate())).thenReturn(Period.ofDays(1));

        showRatingService.stopFutureChecksIfOldOrRecommended(show);

        assertTrue(show.shouldGetRatingChecked());
    }

    @Test
    public void stopFutureChecksIfOldOrRecommended_shouldStopChecking_whenGivenOldShow() {
        Show show = new Show();
        show.setShouldGetRatingChecked(true);
        when(DateUtils.getAge(show.getCreationDate())).thenReturn(Period.ofDays(100));

        showRatingService.stopFutureChecksIfOldOrRecommended(show);

        assertFalse(show.shouldGetRatingChecked());
    }

    @Test
    public void stopFutureChecksIfOldOrRecommended_shouldStopChecking_whenGivenRecommendedShow() {
        Show show = new Show();
        show.setRecommended(true);
        when(DateUtils.getAge(show.getCreationDate())).thenReturn(Period.ofDays(1));

        showRatingService.stopFutureChecksIfOldOrRecommended(show);

        assertFalse(show.shouldGetRatingChecked());
    }

    @Test
    public void checkRating_shouldRecommendShow_givenShowThatMeetsRequirements() {
        List<Show> showsToRecommend = new ArrayList<>();
        List<ShowRating> ratings = List.of(new ShowRating("id25", 10.0, 2000));
        Show show = new Show();
        show.setImdbId("id25");
        when(DateUtils.getAge(show.getCreationDate())).thenReturn(Period.ofDays(1));

        showRatingService.checkRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).hasSize(1);
        assertTrue(show.isRecommended());
        assertFalse(show.shouldGetRatingChecked());
    }

    @Test
    public void checkRating_shouldStopFutureChecks_givenShowThatMeetsOnlyVoteRequirement() {
        List<Show> showsToRecommend = new ArrayList<>();
        List<ShowRating> ratings = List.of(new ShowRating("id25", 2.0, 2000));
        Show show = new Show();
        show.setImdbId("id25");
        when(DateUtils.getAge(show.getCreationDate())).thenReturn(Period.ofDays(1));

        showRatingService.checkRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).isEmpty();
        assertFalse(show.shouldGetRatingChecked());
    }

    @Test
    public void checkRating_shouldCheckRatingInTheFuture_givenShowThatMeetsNoRequirement() {
        List<Show> showsToRecommend = new ArrayList<>();
        List<ShowRating> ratings = List.of(new ShowRating("id25", 2.0, 100));
        Show show = new Show();
        show.setImdbId("id25");
        when(DateUtils.getAge(show.getCreationDate())).thenReturn(Period.ofDays(1));

        showRatingService.checkRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).isEmpty();
        assertTrue(show.shouldGetRatingChecked());
    }

}