package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Before
    public void setUp() {
        showRatingService = new ShowRatingService(imdbProperties, showRepository);
        PowerMockito.mockStatic(DateUtils.class);
        when(imdbProperties.getMaxAge()).thenReturn(30);
        when(imdbProperties.getRequiredRating()).thenReturn((float) 8.0);
        when(imdbProperties.getRequiredVotes()).thenReturn(1000);
    }

    @Test
    public void stopFutureChecksIfOld_shouldStopChecking_givenOldShow() {
        Show show = new Show();
        show.setShouldGetRatingChecked(true);
        when(DateUtils.getAge(show.getReleaseDate())).thenReturn(Period.ofDays(1000));

        showRatingService.stopFutureChecksIfOld(show);

        verify(showRepository).setShouldGetRatingChecked(false, show.getTraktId());
    }

    @Test
    public void stopFutureChecksIfOld_shouldKeepChecking_givenNewShow() {
        Show show = new Show();
        show.setRecommended(true);
        when(DateUtils.getAge(show.getReleaseDate())).thenReturn(Period.ofDays(1));

        showRatingService.stopFutureChecksIfOld(show);

        verifyZeroInteractions(showRepository);
    }

    @Test
    public void checkNewRating_shouldSaveShowForFutureChecks_givenShowThatDoesNotMeetVotesRequirement() {
        Set<Show> showsToRecommend = new HashSet<>();
        List<ShowRating> ratings = List.of(new ShowRating("id1", 0.0, 0));
        Show show = new Show();
        show.setImdbId("id1");

        showRatingService.checkNewRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).isEmpty();
        assertTrue(show.shouldGetRatingChecked());
        verify(showRepository).save(show);
    }

    @Test
    public void checkNewRating_shouldSaveShowForFutureChecks_givenShowThatMeetsRatingAndVotesRequirements() {
        Set<Show> showsToRecommend = new HashSet<>();
        List<ShowRating> ratings = List.of(new ShowRating("id1", 10.0, 5000));
        Show show = new Show();
        show.setImdbId("id1");

        showRatingService.checkNewRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).hasSize(1);
        assertFalse(show.shouldGetRatingChecked());
        verify(showRepository).save(show);
    }

    @Test
    public void checkNewRating_shouldDoNothing_givenShowThatMeetsOnlyVotesRequirement() {
        Set<Show> showsToRecommend = new HashSet<>();
        List<ShowRating> ratings = List.of(new ShowRating("id1", 0.0, 5000));
        Show show = new Show();
        show.setImdbId("id1");
        

        showRatingService.checkNewRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).isEmpty();
        assertFalse(show.shouldGetRatingChecked());
        verifyZeroInteractions(showRepository);
    }

    @Test
    public void checkOldRating_shouldDoNothing_givenShowThatMeetsNoRequirementsAndIsNotOld() {
        Set<Show> showsToRecommend = new HashSet<>();
        List<ShowRating> ratings = List.of(new ShowRating("id1", 0.0, 0));
        Show show = new Show();
        show.setShouldGetRatingChecked(true);
        show.setImdbId("id1");
        when(DateUtils.getAge(show.getReleaseDate())).thenReturn(Period.ofDays(10));

        showRatingService.checkOldRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).isEmpty();
        assertTrue(show.shouldGetRatingChecked());
        verifyZeroInteractions(showRepository);
    }

    @Test
    public void checkOldRating_shouldStopFutureChecks_givenShowThatMeetsNoRequirementsAndIsOld() {
        Set<Show> showsToRecommend = new HashSet<>();
        List<ShowRating> ratings = List.of(new ShowRating("id1", 0.0, 0));
        Show show = new Show();
        show.setImdbId("id1");
        when(DateUtils.getAge(show.getReleaseDate())).thenReturn(Period.ofDays(1000));

        showRatingService.checkOldRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).isEmpty();
        verify(showRepository).setShouldGetRatingChecked(false, show.getTraktId());
        verifyNoMoreInteractions(showRepository);
    }

    @Test
    public void checkOldRating_shouldStopFutureChecks_givenShowThatMeetsRatingAndVotesRequirements() {
        Set<Show> showsToRecommend = new HashSet<>();
        List<ShowRating> ratings = List.of(new ShowRating("id1", 10.0, 5000));
        Show show = new Show();
        show.setImdbId("id1");
        when(DateUtils.getAge(show.getReleaseDate())).thenReturn(Period.ofDays(10));

        showRatingService.checkOldRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).hasSize(1);
        verify(showRepository).setIsRecommended(true, show.getTraktId());
        verify(showRepository).setShouldGetRatingChecked(false, show.getTraktId());
    }

    @Test
    public void checkOldRating_shouldStopFutureChecks_givenShowThatMeetsOnlyVotesRequirementAndIsNotOld() {
        Set<Show> showsToRecommend = new HashSet<>();
        List<ShowRating> ratings = List.of(new ShowRating("id1", 0.0, 5000));
        Show show = new Show();
        show.setImdbId("id1");
        when(DateUtils.getAge(show.getReleaseDate())).thenReturn(Period.ofDays(10));

        showRatingService.checkOldRating(show, ratings, showsToRecommend);

        assertThat(showsToRecommend).hasSize(0);
        verify(showRepository).setShouldGetRatingChecked(false, show.getTraktId());
        verifyNoMoreInteractions(showRepository);
    }

}