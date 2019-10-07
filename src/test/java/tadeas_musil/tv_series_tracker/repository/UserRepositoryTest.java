package tadeas_musil.tv_series_tracker.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
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
        
        joe = new User();
        joe.setUsername("joe@email.com");
        joe.getFollowedShows().add(planetEarth);
        joe = repository.save(joe);

        Show cosmos = new Show();
        cosmos.setTraktId("cosmosId");
        
        User tom = new User();
        tom.setUsername("tom@email.com");
        tom.getFollowedShows().add(cosmos);
        repository.save(tom);
    }
   

    @Test
    public void findByUsername_shouldFindCorrectUser(){
        User user = repository.findByUsername(joe.getUsername());
        
        assertThat(user).isEqualTo(joe);
    }

    @Test
    public void findAllTrackingAtLeastOneShowFetchShows_shouldReturnEmptyList_whenZeroUsersOptInForNotifications(){
        List<String> airingShows = new ArrayList<>();
        airingShows.add("planetEarthId");
        
        List<User> users = repository.findAllTrackingAtLeastOneShowFetchShows(airingShows);
        
        assertThat(users.isEmpty());
    }

    @Test
    public void findAllTrackingAtLeastOneShowFetchShows_shouldFindOneCorrectUser(){
        List<String> airingShows = new ArrayList<>();
        airingShows.add("planetEarthId");
        joe.setDailyScheduleNotification(true);
        
        List<User> users = repository.findAllTrackingAtLeastOneShowFetchShows(airingShows);
        
        assertThat(users).hasSize(1);
        assertThat(users.get(0)).isEqualTo(joe);
    }

}
