package tadeas_musil.tv_series_tracker.scheduled;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.repository.UserRepository;
import tadeas_musil.tv_series_tracker.service.EmailService;
import tadeas_musil.tv_series_tracker.service.ShowService;

@Component
public class ScheduleNotification {

    @Value("${app.schedule_notification.email_subject}")
    private String emailSubject;

    @Autowired
    private ShowService showService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "${cron.daily_schedule_notification}", zone = "${app.timezone}")
    public void notifyUsers() {
        List<Episode> airingEpisodes = showService.getAiringEpisodes();
        List<String> airingShowsIds = airingEpisodes.stream()
                                                    .map(episode -> episode.getShow().getTraktId())
                                                    .collect(Collectors.toList());

        List<User> usersToNotify = userRepository.findByIsGettingScheduleNotificationAndShowIdIn(true, airingShowsIds);

        for (User user : usersToNotify) {
            List<Episode> usersAiringEpisodes = getUsersAiringEpisodes(airingEpisodes, user.getFollowedShows());
            String messageText = emailService.createMessageTextShowsAndEpisodes(usersAiringEpisodes);
            emailService.sendEmail(user.getUsername(), emailSubject, messageText);
        }
    }

    private List<Episode> getUsersAiringEpisodes(List<Episode> airingEpisodes, Set<Show> usersShows) {
        return airingEpisodes.stream()
                            .filter(episode -> usersShows.contains(episode.getShow()))
                            .collect(Collectors.toList());
        
    }
}