package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import tadeas_musil.tv_series_tracker.config_properties.ImdbRatingsProperties;
import tadeas_musil.tv_series_tracker.model.ShowRating;

public class ShowRatingServiceTest {
    
    @InjectMocks
    private ShowRatingService showRatingService;

    @Mock
    private ImdbRatingsProperties imdbProperties;
    
    @Mock
    private FileService fileService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void getShowRating_shouldReturnShowRatingWithZeroRatingAndVotes_whenNoRatingFoundForGivenImdbId(){
        List<ShowRating> ratings = new ArrayList<>();

        ShowRating showRating = showRatingService.getShowRating(ratings, "tt123");

        assertThat(showRating).hasFieldOrPropertyWithValue("imdbId", "tt123")
                            .hasFieldOrPropertyWithValue("rating", 0.0)
                            .hasFieldOrPropertyWithValue("numberOfVotes", 0);
    }

    @Test
    public void getShowRating_shouldReturnCorrectShowRating(){
        ShowRating showRating = new ShowRating("tt123", 7.5, 1000);
        List<ShowRating> ratings = List.of(showRating);

        ShowRating result = showRatingService.getShowRating(ratings, "tt123");

        assertThat(result).hasFieldOrPropertyWithValue("imdbId", "tt123")
                            .hasFieldOrPropertyWithValue("rating", 7.5)
                            .hasFieldOrPropertyWithValue("numberOfVotes", 1000);
    }
  
    @Test
    public void meetsRequiredVotes_shouldReturnTrue_givenRatingWithEnoughVotes(){
        ShowRating showRating = new ShowRating("tt123", 7.5, 1000);
        when(imdbProperties.getRequiredVotes()).thenReturn(5);

        boolean meetsRequiredVotes = showRatingService.meetsRequiredVotes(showRating);

        assertTrue(meetsRequiredVotes);
    }

    @Test
    public void meetsRequiredVotes_shouldReturnFalse_givenRatingWithoutEnoughVotes(){
        ShowRating showRating = new ShowRating("tt123", 7.5, 50);
        when(imdbProperties.getRequiredVotes()).thenReturn(1000);

        boolean meetsRequiredVotes = showRatingService.meetsRequiredVotes(showRating);

        assertFalse(meetsRequiredVotes);
    }

    @Test
    public void meetsRequiredRatingAndVotes_shouldReturnTrue_givenRatingThatMeetsRequirements(){
        ShowRating showRating = new ShowRating("tt123", 10.0, 1000);
        when(imdbProperties.getRequiredVotes()).thenReturn(5);
        when(imdbProperties.getRequiredRating()).thenReturn((float) 6.0);
        
        boolean meetsRequiredVotes = showRatingService.meetsRequiredRatingAndVotes(showRating);

        assertTrue(meetsRequiredVotes);
    }

    @Test
    public void meetsRequiredRatingAndVotes_shouldReturnFalse_givenRatingThatDoesNotMeetAnyRequirements(){
        ShowRating showRating = new ShowRating("tt123", 0.0, 50);
        when(imdbProperties.getRequiredVotes()).thenReturn(1000);
        when(imdbProperties.getRequiredRating()).thenReturn((float) 8.0);
        
        boolean meetsRequiredVotes = showRatingService.meetsRequiredRatingAndVotes(showRating);

        assertFalse(meetsRequiredVotes);
    }

    @Test
    public void meetsRequiredRatingAndVotes_shouldReturnFalse_givenRatingThatMeetsOnlyVotesRequirement(){
        ShowRating showRating = new ShowRating("tt123", 0.0, 1000);
        when(imdbProperties.getRequiredVotes()).thenReturn(5);
        when(imdbProperties.getRequiredRating()).thenReturn((float) 8.0);
        
        boolean meetsRequiredVotes = showRatingService.meetsRequiredRatingAndVotes(showRating);

        assertFalse(meetsRequiredVotes);
    }



}