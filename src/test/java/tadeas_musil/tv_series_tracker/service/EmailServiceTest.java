package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;


public class EmailServiceTest {
  
  private EmailService emailService = new EmailService();

  @Test
  public void createMessageTextShows_shouldReturnCorrectMessage_givenTwoShows(){
    Show planetEarth = new Show();
    planetEarth.setTitle("Planet Earth");
    planetEarth.setYear(2015);
    Show cosmos = new Show();
    cosmos.setTitle("Cosmos");
    cosmos.setYear(2007);
    List<Show> shows = List.of(planetEarth,cosmos);
    
    String message = emailService.createMessageTextShows(shows);
    
    assertThat(message).isEqualTo("Planet Earth (2015)\n\nCosmos (2007)\n\n");

  }

  @Test
  public void createMessageTextShowsAndEpisodes_shouldReturnCorrectMessage_givenTwoEpisodes(){
    Show planetEarth = new Show();
    planetEarth.setTitle("Planet Earth");
    planetEarth.setYear(2015);
    Episode planetEarthEpisode = new Episode(planetEarth, "01", "01", "Pilot", "21.12.2006");
    Show cosmos = new Show();
    cosmos.setTitle("Cosmos");
    cosmos.setYear(2007);
    Episode cosmosEpisode = new Episode(cosmos, "01", "01", "Pilot", "15.2.2014");
    List<Episode> episodes = List.of(planetEarthEpisode,cosmosEpisode);
    
    String message = emailService.createMessageTextShowsAndEpisodes(episodes);
    
    assertThat(message).isEqualTo("Planet Earth(2015): S01E01 Pilot 21.12.2006\n\nCosmos(2007): S01E01 Pilot 15.2.2014\n\n");

  }

}