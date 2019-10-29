package tadeas_musil.tv_series_tracker.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.config_properties.ImdbRatingsProperties;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.model.comparator.ShowComparator;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.util.DateUtils;

@Service
public class ShowRatingService {

    @Autowired
    private ImdbRatingsProperties imdbProperties;

    @Autowired
    private ShowRepository showRepository;

    public ShowRating getShowRating(List<ShowRating> ratings, String imdbId) {
        int index = Collections.binarySearch(ratings, new ShowRating(imdbId));
        if (index < 0) {
            return new ShowRating(imdbId, 0, 0);
        }
        return ratings.get(index);
    }

    private boolean meetsRequiredRatingAndVotes(ShowRating rating) {
        return rating.getNumberOfVotes() >= imdbProperties.getRequiredVotes()
                && rating.getRating() >= imdbProperties.getRequiredRating();
    }

    private boolean meetsRequiredVotes(ShowRating rating) {
        return rating.getNumberOfVotes() >= imdbProperties.getRequiredVotes();
    }

    public void checkOldRating(Show show, List<ShowRating> ratings, Set<Show> recommendedShows) {
        ShowRating showRating = getShowRating(ratings, show.getImdbId());

        if (meetsRequiredRatingAndVotes(showRating)) {
            showRepository.setIsRecommended(true, show.getTraktId());
            showRepository.setShouldGetRatingChecked(false, show.getTraktId());
            recommendedShows.add(show);
        } else if (meetsRequiredVotes(showRating)) {
            showRepository.setShouldGetRatingChecked(false, show.getTraktId());
        } else {
            stopFutureChecksIfOld(show);
        }
    }

    public void checkNewRating(Show show, List<ShowRating> ratings, Set<Show> recommendedShows) {
        ShowRating showRating = getShowRating(ratings, show.getImdbId());

        if (meetsRequiredRatingAndVotes(showRating)) {
            show.setRecommended(true);
            showRepository.save(show);
            recommendedShows.add(show);
        } else if (!meetsRequiredVotes(showRating)) {
            show.setShouldGetRatingChecked(true);
            showRepository.save(show);
        }
    }

    public void stopFutureChecksIfOld(Show show) {
        int ageInDays = DateUtils.getAge(show.getReleaseDate()).getDays();
        if (ageInDays > imdbProperties.getMaxAge()) {
            showRepository.setShouldGetRatingChecked(false, show.getTraktId());
        }
    }

    public Set<Show> checkRatings(List<Show> shows, List<ShowRating> ratings) {
        // Using TreeSet with comparator to filter duplicates, because TraktTV sometimes has duplicates of the same show
        Set<Show> recommendedShows = new TreeSet<>(new ShowComparator());
        for (Show show : shows) {
            if (showRepository.existsById(show.getTraktId())) {
                checkOldRating(show, ratings, recommendedShows);
            }
            else{
                checkNewRating(show, ratings, recommendedShows);
            }
        }
        return recommendedShows;
    }

    public ShowRatingService(ImdbRatingsProperties imdbProperties, ShowRepository showRepository) {
        this.imdbProperties = imdbProperties;
        this.showRepository = showRepository;
    }

}