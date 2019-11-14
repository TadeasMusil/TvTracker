package tadeas_musil.tv_series_tracker.scheduled;

import java.util.List;
import java.util.Set;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.repository.UserRepository;
import tadeas_musil.tv_series_tracker.service.EmailService;
import tadeas_musil.tv_series_tracker.service.ShowRatingService;
import tadeas_musil.tv_series_tracker.service.ShowService;

@Component
public class RecommendedShowsNotification {

    @Autowired
    private ShowService showService;

    @Value("${app.scheduled.new_show_notification.subject}")
    private String emailSubject;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ShowRatingService showRatingService;

    @Scheduled(cron = "${cron.recommended_shows_notification}", zone = "${app.timezone}")
    public void notifyUsers() {
        Set<Show> recommendedShows = findNewRecommendations(showRatingService.getRatings());

        if (CollectionUtils.isNotEmpty(recommendedShows)) {
            List<String> usersToNotify = userRepository.findByIsGettingRecommendedShowsNotification(true);
            String messageText = emailService.createMessageTextShows(recommendedShows);

            for (String email : usersToNotify) {
                emailService.sendEmail(email, emailSubject, messageText);
            }
        }
    }

    private Set<Show> findNewRecommendations(List<ShowRating> ratings) {
        List<Show> premieringShows = showService.getPremieringShows();
        handleSpecialCases(premieringShows);
        
        List<Show> existingShowsToCheck = showRepository.findAllByShouldGetRatingChecked(true);

        Set<Show> recommendedShows = showService.findRecommendedShows(premieringShows, ratings);
        recommendedShows.addAll(showService.findRecommendedShows(existingShowsToCheck, ratings));
        return recommendedShows;
    }

    //Handles shows that are already saved in the database, but are yet to be released
    private void handleSpecialCases(List<Show> premieringShows) {
        for (Show show : premieringShows) {
            if(showRepository.existsById(show.getTraktId())){
                showRepository.setReleaseDate(show.getReleaseDate(), show.getTraktId());
                showRepository.setShouldGetRatingChecked(true, show.getTraktId());
            }
        }
    }
}