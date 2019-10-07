package tadeas_musil.tv_series_tracker.scheduled;

import java.util.List;
import java.util.stream.Collectors;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.repository.UserRepository;
import tadeas_musil.tv_series_tracker.service.ShowService;

@Component
public class DailyScheduleNotification  {
 
    @Autowired
    private ShowService showService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    private List<Episode> airingEpisodes;

@Scheduled(cron = "${cron.daily.schedule.notification}", zone = "${app.timezone}")
public void notifyUsers() throws Exception{
            airingEpisodes = showService.getAiringEpisodes();
            List<String> showIds = airingEpisodes
                                                .stream()
                                                .map(e -> e.getShow().getTraktId())
                                                .collect(Collectors.toList());
                    
            List<User> users = userRepository.findAllTrackingAtLeastOneShowFetchShows(showIds);

                    for (User user : users) {
                        sendEmail(user);
                    }
}

private void sendEmail(User user) throws Exception{
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setRecipients(RecipientType.TO, user.getUsername());
            message.setFrom(new InternetAddress("${email.address.sender}", "TV Tracker"));
            message.setSubject("Here are your episodes that air today!");
            message.setText(createMessageText(user));
            javaMailSender.send(message);
}

public String createMessageText(User user){
    StringBuilder builder = new StringBuilder();
    for (Show show : user.getFollowedShows()) {
        getAiringEpisodes(show).forEach(episode ->
                                                    builder
                                                    .append(show.getTitle())
                                                    .append(": S")
                                                    .append(episode.getSeason())
                                                    .append("E")
                                                    .append(episode.getNumber())
                                                    .append(" ")
                                                    .append(episode.getTitle())
                                                    .append(" ")
                                                    .append(episode.getAirDate())
                                                    .append("\n\n"));
    }
    return builder.toString();
}
private List<Episode> getAiringEpisodes(Show show){
return airingEpisodes.stream()
                     .filter(episode -> episode.getShow().getTraktId().equals(show.getTraktId()))
                     .collect(Collectors.toList());
}
}