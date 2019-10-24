package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceTest {
    
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @Before
    public void setUp(){
        when(passwordEncoder.encode(anyString())).then(returnsFirstArg());
    }
    private User getTestUser(){
        User user = new User();
        user.setUsername("test@email.com");
        user.setPassword("password");
        return user;
    }
    @Test
    public void saveUser_shouldSaveUser() {
        User user = getTestUser();

        userService.saveUser(user);
        User savedUser = userRepository.findByUsername(user.getUsername());
        
        assertThat(savedUser).hasFieldOrPropertyWithValue("username", user.getUsername())
                             .hasFieldOrPropertyWithValue("password", user.getPassword())
                             .hasFieldOrPropertyWithValue("isGettingScheduleNotification", true)
                             .hasFieldOrPropertyWithValue("isGettingRecommendedShowsNotification", true);

    }

    @Test
    public void updateUser_shouldUpdateUser() {
        User user = getTestUser();
        userRepository.save(user);
        user.setPassword("newPassword");
        user.setGettingScheduleNotification(false);
        
        userService.updateUser(user);
        User updatedUser = userRepository.findByUsername(user.getUsername());
        
        assertThat(updatedUser).hasFieldOrPropertyWithValue("password", "newPassword")
                               .hasFieldOrPropertyWithValue("isGettingScheduleNotification", false);

    }

    @Test
    public void followShow_shouldAddShow() {
        Show showToAdd = new Show();
        showToAdd.setTraktId("showId");
        showRepository.save(showToAdd);
        
        User user = getTestUser();
        userRepository.save(user);
        
        userService.followShow(user.getUsername(), showToAdd.getTraktId());
        user = userRepository.findByUsernameFetchShows(user.getUsername());
        
        assertThat(user.getFollowedShows()).hasSize(1); 

    }

    @Test
    public void unfollowShow_shouldUnfollowShow() {
        Show show = new Show();
        show.setTraktId("traktId");
        User user = getTestUser();
        user.getFollowedShows().add(show);
        userRepository.save(user);
        
        
        userService.unfollowShow(user.getUsername(), show.getTraktId());
        user = userRepository.findByUsernameFetchShows(user.getUsername());
        
        assertThat(user.getFollowedShows()).isEmpty(); 
    }
}
