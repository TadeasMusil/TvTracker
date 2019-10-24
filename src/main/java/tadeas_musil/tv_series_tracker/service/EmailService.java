package tadeas_musil.tv_series_tracker.service;

import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;

@Service
public class EmailService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String username, String subject, String text) {
      try{  MimeMessage message = javaMailSender.createMimeMessage();
        message.setRecipients(RecipientType.TO, username);
        message.setFrom(new InternetAddress("TVTracker", "TV Tracker"));
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
      }
      catch(Exception e){
          logger.error("Failed to send email to {}", username, e);
      }
    }

    public String createMessageTextShowsAndEpisodes(List<Episode> episodes) {
        StringBuilder builder = new StringBuilder();

        episodes.forEach(episode -> builder
                                    .append(episode.getShow().getTitle())
                                    .append("(")
                                    .append(episode.getShow().getYear())
                                    .append(")")
                                    .append(": S")
                                    .append(episode.getSeason())
                                    .append("E")
                                    .append(episode.getNumber())
                                    .append(" ")
                                    .append(episode.getTitle())
                                    .append(" ")
                                    .append(episode.getAirDate())
                                    .append("\n\n"));

        return builder.toString();
    }

    public String createMessageTextShows(List<Show> shows) {
        StringBuilder builder = new StringBuilder();
        shows.forEach(show -> builder
                        .append(show.getTitle())
                        .append(" (")
                        .append(show.getYear())
                        .append(")")
                        .append("\n\n"));

        return builder.toString();
    }
}