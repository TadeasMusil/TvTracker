package tadeas_musil.tv_series_tracker.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.mail.internet.MimeMessage;

import com.google.common.collect.Sets;
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

import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowRating;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.repository.UserRepository;
import tadeas_musil.tv_series_tracker.service.FileService;
import tadeas_musil.tv_series_tracker.service.ShowRatingService;
import tadeas_musil.tv_series_tracker.service.ShowService;

@SpringBootTest(properties = { "spring.mail.host=localhost",
                                "spring.mail.port=3025",
                                "spring.mail.username=username",
                                "spring.mail.password=password" })
@RunWith(SpringRunner.class)
public class RecommendedShowsNotificationTest {

    @Autowired
    private RecommendedShowsNotification recommendedShowsNotification;

    @MockBean
    private ShowService showService;

    @MockBean
    private FileService fileService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ShowRepository showRepository;

    @MockBean
    private ShowRatingService showRatingService;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Before
    public void setUp() {
        greenMail.setUser("test@localhost", "username", "password");

        when(fileService.parseTsv(anyString())).thenReturn(List.of(new ShowRating("id1", 10.0, 2000)));

        
        when(showService.getPremieringShows()).thenReturn(Arrays.asList(getPlanetEarth()));

        User user = new User();
        user.setUsername("test@localhost");
        when(userRepository.findByIsGettingRecommendedShowsNotification(true)).thenReturn(List.of(user.getUsername()));

    }
    
    private Show getPlanetEarth(){
        Show planetEarth = new Show();
        planetEarth.setTitle("Planet Earth");
        planetEarth.setImdbId("id1");
        planetEarth.setYear(2015);
        return planetEarth;
    }

    @Test
    public void notifyUsers_shouldSendCorrectEmail_givenOneNewRecommendedShow() throws Exception {
        when(showRatingService.checkRatings(anyList(), anyList())).thenReturn(Sets.newHashSet(getPlanetEarth()));

        recommendedShowsNotification.notifyUsers();
        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertThat(messages).hasSize(1);
        assertThat(GreenMailUtil.getBody(messages[0])).isEqualTo("Planet Earth (2015)");
        assertThat(messages[0].getSubject()).isEqualTo("New recommendations got discovered!");
    }

    @Test
    public void notifyUsers_shouldSendZeroEmails_givenNoNewRecommendedShow() throws Exception {
        when(showRatingService.checkRatings(anyList(), anyList())).thenReturn(new HashSet<Show>());

        recommendedShowsNotification.notifyUsers();
        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertThat(messages).isEmpty();

    }

}