package tadeas_musil.tv_series_tracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import tadeas_musil.tv_series_tracker.model.Show;
@RunWith(SpringRunner.class)
@DataJpaTest
public class ShowRepositoryTest {
    
    @Autowired
    private ShowRepository repository;

    @Before
    public void setUp(){
        Show planetEarth = new Show();
        planetEarth.setTraktId("planetEarthId");
        planetEarth.setShouldGetRatingChecked(true);
        repository.save(planetEarth);
        
        
        Show cosmos = new Show();
        cosmos.setTraktId("cosmosId");
        cosmos.setShouldGetRatingChecked(false);
        repository.save(cosmos);
    }   
   

    @Test
    public void findAllByShouldGetRatingChecked_shouldFindCorrectShow(){
        List<Show> shows = repository.findAllByShouldGetRatingChecked(true);
        
        assertThat(shows).hasSize(1)
                        .first().hasFieldOrPropertyWithValue("traktId", "planetEarthId");
                        
    }

}
