package tadeas_musil.tv_series_tracker.scheduled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.repository.UserRepository;
import tadeas_musil.tv_series_tracker.service.ShowService;

@SpringBootTest(properties = { "spring.mail.host=localhost",
                                "spring.mail.port=3025",
                                "spring.mail.username=username",
                                "spring.mail.password=password" })
@RunWith(SpringRunner.class)
public class DailyScheduleNotificationTest {
    @Autowired
    private DailyScheduleNotification dailyScheduleNotification;
    @MockBean
    private ShowService showService;
    @MockBean
    private UserRepository userRepository;
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);
    

    @Before
    public void setUp() {
        greenMail.setUser("test@localhost", "username", "password");
        Show planetEarth = new Show();
        planetEarth.setTraktId("id");
        planetEarth.setTitle("Planet Earth");
        
        Episode planetEarthEpisode = new Episode();
        planetEarthEpisode.setAirDate("21.12.2006");
        planetEarthEpisode.setNumber("01");
        planetEarthEpisode.setSeason("01");
        planetEarthEpisode.setTitle("Pilot");
        planetEarthEpisode.setShow(planetEarth);
        
        User user = new User();
        user.setUsername("test@localhost");
        user.getFollowedShows().add(planetEarth);
        when(userRepository.findAllTrackingAtLeastOneShowFetchShows(any())).thenReturn(List.of(user));
        
        List<Episode> airingEpisodes = List.of(planetEarthEpisode);
        when(showService.getAiringEpisodes()).thenReturn(airingEpisodes);
    }

    @Test
    public void shouldSendCorrectEmail() throws Exception {
        dailyScheduleNotification.notifyUsers();
        MimeMessage[] messages = greenMail.getReceivedMessages();
        
        assertThat(messages).hasSize(1);
        assertThat(GreenMailUtil.getBody(messages[0])).isEqualTo("Planet Earth: S01E01 Pilot 21.12.2006");
        assertThat(messages[0].getSubject()).isEqualTo("Here are your episodes that air today!");
    }

}