package tadeas_musil.tv_series_tracker.scheduled;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tadeas_musil.tv_series_tracker.config_properties.ImdbRatingsProperties;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.repository.UserRepository;
import tadeas_musil.tv_series_tracker.service.EmailService;
import tadeas_musil.tv_series_tracker.service.FileService;
import tadeas_musil.tv_series_tracker.service.ShowRatingService;
import tadeas_musil.tv_series_tracker.service.ShowService;

@Component
public class RecommendedShowsNotification {

    @Autowired
    private FileService fileService;

    @Autowired
    private ShowService showService;

    @Autowired
    private ImdbRatingsProperties imdbProperties;

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
        Set<Show> recommendedShows = findNewRecommendations(getRatings());

        if (CollectionUtils.isNotEmpty(recommendedShows)) {
            List<String> usersToNotify = userRepository.findByIsGettingRecommendedShowsNotification(true);
            String messageText = emailService.createMessageTextShows(recommendedShows);

            for (String username : usersToNotify) {
                emailService.sendEmail(username, emailSubject, messageText);
            }
        }
    }

    private List<ShowRating> getRatings() {
        fileService.downloadFile(imdbProperties.getUrl(), imdbProperties.getDownloadLocation());
        fileService.decompressGzip(imdbProperties.getDownloadLocation(), imdbProperties.getDecompressionLocation());
        List<ShowRating> ratings = fileService.parseTsv(imdbProperties.getDecompressionLocation());
        deleteFiles();
        return ratings;
    }

    private Set<Show> findNewRecommendations(List<ShowRating> ratings) {
        List<Show> premieringShows = showService.getPremieringShows();
        premieringShows.forEach(show -> showService.setReleaseDateForExistingShow(show.getReleaseDate(),show.getTraktId()));

        List<Show> existingShowsToCheck = showRepository.findAllByShouldGetRatingChecked(true);

        Set<Show> recommendedShows = showRatingService.checkRatings(premieringShows, ratings);
        recommendedShows.addAll(showRatingService.checkRatings(existingShowsToCheck, ratings));
        return recommendedShows;
    }

    private void deleteFiles() {
        File downloadedFile = new File(imdbProperties.getDownloadLocation());
        downloadedFile.delete();
        File decompressedFile = new File(imdbProperties.getDecompressionLocation());
        decompressedFile.delete();
    }
}