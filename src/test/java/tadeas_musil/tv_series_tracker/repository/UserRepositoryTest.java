package tadeas_musil.tv_series_tracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {
    
    @Autowired
    private UserRepository repository;

    private User joe;

    @Before
    public void setUp(){
        Show planetEarth = new Show();
        planetEarth.setTraktId("planetEarthId");
        Show cosmos = new Show();
        cosmos.setTraktId("cosmosId");
        
        joe = new User();
        joe.setGettingScheduleNotification(true);
        joe.setUsername("joe@email.com");
        joe.getFollowedShows().add(planetEarth);
        joe.getFollowedShows().add(cosmos);
        joe = repository.save(joe);

        User tom = new User();
        tom.setUsername("tom@email.com");
        repository.save(tom);
    }
   

    @Test
    public void findByUsername_shouldFindCorrectUser(){
        User user = repository.findByUsername(joe.getUsername());
        
        assertThat(user).isEqualTo(joe);
    }

    @Test
    public void findByIsGettingScheduleNotificationAndShowIdIn_shouldReturnCorrectUser(){
        List<String> airingShows = List.of("planetEarthId");
        
        List<User> users = repository.findByIsGettingScheduleNotificationAndShowIdIn(true, airingShows);
        
        assertThat(users).hasSize(1)
                         .first().isEqualTo(joe);
    }

    @Test
    public void findByIsGettingScheduleNotificationAndShowIdIn_shouldReturnEmptyList_whenZeroUsersOptInForNotifications(){
        List<String> airingShows = List.of("planetEarthId");
        joe.setGettingScheduleNotification(false);
        
        List<User> users = repository.findByIsGettingScheduleNotificationAndShowIdIn(true, airingShows);
        
        assertThat(users).isEmpty();
    }

     @Test
    public void findByIsGettingScheduleNotificationAndShowIdIn_shouldReturnEmptyList_givenEmptyShowIdList(){
        List<String> airingShows = new ArrayList<>();
        
        List<User> users = repository.findByIsGettingScheduleNotificationAndShowIdIn(true, airingShows);
        
        assertThat(users).isEmpty();
    }

    @Test
    public void findByIsGettingRecommendedShowsNotification_shouldFindTwoUsers_whenTwoUsersOptInForNotifications(){        
        List<String> users = repository.findByIsGettingRecommendedShowsNotification(true);
        
        assertThat(users).hasSize(2);
    }

}
