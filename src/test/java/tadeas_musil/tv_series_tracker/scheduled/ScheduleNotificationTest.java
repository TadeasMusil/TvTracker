package tadeas_musil.tv_series_tracker.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;

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

@SpringBootTest(properties = {  "spring.mail.host=localhost",
                                "spring.mail.port=3025",
                                "spring.mail.username=username",
                                "spring.mail.password=password" })

@RunWith(SpringRunner.class)
public class ScheduleNotificationTest {
    @Autowired
    private ScheduleNotification dailyScheduleNotification;
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
        planetEarth.setTitle("Planet Earth");
        planetEarth.setYear(2015);
        
        Episode planetEarthEpisode = new Episode(planetEarth, "01", "01", "Pilot", "21.12.2006");

        List<Episode> airingEpisodes = List.of(planetEarthEpisode);
        when(showService.getAiringEpisodes()).thenReturn(airingEpisodes);
        
        User user = new User();
        user.setUsername("test@localhost");
        user.getFollowedShows().add(planetEarth);
        when(userRepository.findByIsGettingScheduleNotificationAndShowIdIn(anyBoolean(),anyList())).thenReturn(List.of(user));
    }

    @Test
    public void notifyUsers_shouldSendCorrectEmail() throws Exception {
        dailyScheduleNotification.notifyUsers();
        MimeMessage[] messages = greenMail.getReceivedMessages();
        
        assertThat(messages).hasSize(1);
        assertThat(GreenMailUtil.getBody(messages[0])).isEqualTo("Planet Earth(2015): S01E01 Pilot 21.12.2006");
        assertThat(messages[0].getSubject()).isEqualTo("Here are your episodes that air today!");
    }

}