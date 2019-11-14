package tadeas_musil.tv_series_tracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.api.TraktTvApi;
import tadeas_musil.tv_series_tracker.api.TvMazeApi;
import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.model.ShowsPage;
import tadeas_musil.tv_series_tracker.model.comparator.ShowComparator;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.util.DateUtils;

@Service
public class ShowService {
    
    @Value("${app.shows_per_page}")
    private int showsPerPage;
    
    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private TraktTvApi traktTvApi;

    @Autowired
    private TvMazeApi tvMazeApi;

    @Autowired
    private ShowRatingService showRatingService;
    
    @Value("${app.shows_per_page}")
    private int showMaxAge;

    public Show findShow(String traktId){
        return traktTvApi.findShowById(traktId);
    }

    public List<Episode> getAiringEpisodes(){
        return traktTvApi.getAiringEpisodes();
    }

    public List<Show> getPremieringShows(){
        List<Show> premieringShows = traktTvApi.getPremieringShows();
        premieringShows.removeIf(show -> show.getImdbId() == null || !show.getImdbId().startsWith("tt")); 
        premieringShows.forEach(show -> show.setReleaseDate(DateUtils.getCurrentDate()));
        
        return premieringShows;
    }
    
    // Adding missing images here to preserve memory, because heroku 
    // quotas were getting exceeded during recommended shows notification
    public Page<Show> getRecommendedShows(int pageNumber){
        List<Show> showsWithoutImage = showRepository.findByIsRecommendedAndImageUrl(true, null);
        showsWithoutImage.forEach(show -> showRepository.setImageUrl(getImageUrl(show.getTvdbId()), show.getTraktId()));
        
        Pageable pageable = PageRequest.of(pageNumber, showsPerPage);
        Page<Show> page = showRepository.findByIsRecommendedOrderByReleaseDateDesc(true, pageable);
        return page;
    }

    public String getImageUrl(String tvdbId) {
        return tvMazeApi.getImageUrl(tvdbId);
              
    }

    public void addImages(List<Show> shows) {
        for (Show show : shows) {
            String tvdbId = show.getTvdbId();
            if (tvdbId != null) {
                String url = getImageUrl(tvdbId);
                show.setImageUrl(url);
            }
        }
    }

    public ShowsPage searchShows(String query, Integer page) {
        ShowsPage shows = traktTvApi.searchShowsByQuery(query, page);
        addImages(shows.getShows());

        return shows;
    }

/**
 * Checks rating of an existing show and returns true if it passes all requirements.
 * Show's rating won't be checked again if show is too old or meets only votes requirements.
 */
    public boolean checkRatingOfExistingShow(Show show, List<ShowRating> ratings) {
        ShowRating showRating = showRatingService.getShowRating(ratings, show.getImdbId());

        if (showRatingService.meetsRequiredRatingAndVotes(showRating)) {
            showRepository.setIsRecommended(true, show.getTraktId());
            showRepository.setShouldGetRatingChecked(false, show.getTraktId());
            return true;
        } else if (showRatingService.meetsRequiredVotes(showRating)) {
            showRepository.setShouldGetRatingChecked(false, show.getTraktId());
        } else {
            stopCheckingRatingIfOld(show);
        }
        return false;
    }

    private void stopCheckingRatingIfOld(Show show) {
        int ageInDays = DateUtils.getAge(show.getReleaseDate()).getDays();
        if (ageInDays > showMaxAge) {
            showRepository.setShouldGetRatingChecked(false, show.getTraktId());
        }
    }
/**
 * Checks rating of a new show and returns true if it passes all requirements.
 */
    public boolean checkRatingOfNewShow(Show show, List<ShowRating> ratings) {
        ShowRating showRating = showRatingService.getShowRating(ratings, show.getImdbId());

        if (showRatingService.meetsRequiredRatingAndVotes(showRating)) {
            show.setRecommended(true);
            showRepository.save(show);
            return true;
        } 
        else if (!showRatingService.meetsRequiredVotes(showRating)) {
            show.setShouldGetRatingChecked(true);
            showRepository.save(show);
        }
        return false;
    }

    public Set<Show> findRecommendedShows(List<Show> shows, List<ShowRating> ratings) {
        // Using TreeSet with comparator to filter duplicates, because TraktTV sometimes has duplicates of the same show
        Set<Show> recommendedShows = new TreeSet<>(new ShowComparator());
        boolean isRecommended = false;
        for (Show show : shows) {
            if (showRepository.existsById(show.getTraktId())) {
                isRecommended = checkRatingOfExistingShow(show, ratings);
            }
            else{
                isRecommended = checkRatingOfNewShow(show, ratings);
            }
            if(isRecommended){
                recommendedShows.add(show);
            }
        }
        return recommendedShows;
    }
}