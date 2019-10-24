package tadeas_musil.tv_series_tracker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.config_properties.ImdbRatingsProperties;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.util.DateUtils;

@Service
public class ShowRatingService {

    @Autowired
    private ImdbRatingsProperties imdbProperties;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowService showService;

    @Value("${app.timezone}")
    private String timezone;

    public ShowRating getShowRating(List<ShowRating> ratings, String imdbId) {
        int index = Collections.binarySearch(ratings, new ShowRating(imdbId));
        return ratings.get(index);
    }

    private boolean meetsRequiredRatingAndVotes(ShowRating rating) {
        return rating.getNumberOfVotes() >= imdbProperties.getRequiredVotes()
                && rating.getRating() >= imdbProperties.getRequiredRating();
    }

    private boolean meetsRequiredVotes(ShowRating rating) {
        return rating.getNumberOfVotes() >= imdbProperties.getRequiredVotes();
    }

    public void checkRating(Show show, List<ShowRating> ratings, List<Show> showsToRecommend) {
        ShowRating showRating = getShowRating(ratings, show.getImdbId());

        if (meetsRequiredRatingAndVotes(showRating)) {
            show.setRecommended(true);
            showsToRecommend.add(show);
        } else if (meetsRequiredVotes(showRating)) {
            show.setShouldGetRatingChecked(false);
            return;
        }
        show.setShouldGetRatingChecked(true);
        stopFutureChecksIfOldOrRecommended(show);
        showService.saveNewShow(show);

    }

    public void stopFutureChecksIfOldOrRecommended(Show show) {
        int ageInDays = DateUtils.getAge(show.getCreationDate()).getDays();
        if (ageInDays > imdbProperties.getMaxAge() || show.isRecommended()) {
            show.setShouldGetRatingChecked(false);
            showRepository.save(show);
        }
    }

    public List<Show> checkRatings(List<Show> shows, List<ShowRating> ratings) {
        List<Show> showsToRecommend = new ArrayList<>();
        shows.forEach(show -> checkRating(show, ratings, showsToRecommend));
        return showsToRecommend;
    }

    public ShowRatingService(ImdbRatingsProperties imdbProperties, ShowRepository showRepository,
            ShowService showService) {
        this.imdbProperties = imdbProperties;
        this.showRepository = showRepository;
        this.showService = showService;
    }

}