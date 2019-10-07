package tadeas_musil.tv_series_tracker.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;



import static org.mockito.AdditionalAnswers.returnsFirstArg;

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

    private User joe; 

    @Before
    public void setUp(){
        joe = new User();
        joe.setUsername("joe@email.com");
        joe.setPassword("password");
        userRepository.save(joe);
        when(passwordEncoder.encode(anyString())).then(returnsFirstArg());
    }
    @Test
    public void saveUser_shouldSaveUser() {
        userService.saveUser(joe);
        
        User savedUser = userRepository.findByUsername(joe.getUsername());
        
        assertThat(savedUser).hasFieldOrPropertyWithValue("username", joe.getUsername())
                             .hasFieldOrPropertyWithValue("password", joe.getPassword())
                             .hasFieldOrPropertyWithValue("dailyScheduleNotification", true)
                             .hasFieldOrPropertyWithValue("newShowNotification", true);

    }

    @Test
    public void updateUser_shouldUpdateUser() {
        joe.setPassword("newPassword");
        joe.setConfirmPassword("newPassword");
        joe.setDailyScheduleNotification(true);
        
        userService.updateUser(joe);
        User updatedUser = userRepository.findByUsername(joe.getUsername());
        
        assertThat(updatedUser).hasFieldOrPropertyWithValue("password", joe.getPassword())
                                .hasFieldOrPropertyWithValue("dailyScheduleNotification", joe.isDailyScheduleNotification());

    }

    @Test
    public void getByUsername_shouldReturnCorrectUser() {
        User searchResult = userService.getByUsername(joe.getUsername());
        
        assertThat(searchResult).hasFieldOrPropertyWithValue("username", joe.getUsername())
                                .hasFieldOrPropertyWithValue("password", joe.getPassword());

    }

    @Test
    public void followShow_shouldFollowShow() {
        Show showToAdd = new Show();
        showToAdd.setTraktId("traktId");
        showToAdd.setYear(1990);
        showToAdd = showRepository.save(showToAdd);
        
        userService.followShow(joe.getUsername(), showToAdd.getTraktId());
        User user = userRepository.findByUsernameFetchShows(joe.getUsername());
        
        assertThat(user.getFollowedShows()).hasSize(1); 

    }

    @Test
    public void unfollowShow_shouldUnfollowShow() {
        Show show = new Show();
        show.setTraktId("traktId");
        show.setYear(1990);
        joe.getFollowedShows().add(show);
        userRepository.save(joe);
        
        userService.unfollowShow(joe.getUsername(), show.getTraktId());
        User user = userRepository.findByUsernameFetchShows(joe.getUsername());
        
        assertThat(user.getFollowedShows()).isEmpty(); 
    }
}
