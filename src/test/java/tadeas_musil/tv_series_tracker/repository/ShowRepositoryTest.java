package tadeas_musil.tv_series_tracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
    private ShowRepository showRepository; 
   
    @Test
    public void findAllByShouldGetRatingChecked_shouldFindOneShow(){
        Show show1 = new Show();
        show1.setTraktId("1");
        Show show2 = new Show();
        show2.setTraktId("2");
        show2.setShouldGetRatingChecked(true);
        showRepository.saveAll(List.of(show1, show2));
        
        List<Show> shows = showRepository.findAllByShouldGetRatingChecked(true);
        
        assertThat(shows).hasSize(1);       
    }

    @Test
    public void setReleaseDate_shouldUpdateReleaseDate(){
        Show show = new Show();
        show.setTraktId("id");
        showRepository.save(show);
        LocalDate date = LocalDate.of(2015,05,05);
        
        showRepository.setReleaseDate(date, "id");
        Show updatedShow = showRepository.findById("id").get();
        
        assertThat(updatedShow).hasFieldOrPropertyWithValue("releaseDate", date);            
    }

    @Test
    public void setShouldGetRatingChecked_shouldUpdateShouldGetRatingChecked(){
        Show show = new Show();
        show.setTraktId("id");
        showRepository.save(show);

        showRepository.setShouldGetRatingChecked(true, "id");
        Show updatedShow = showRepository.findById("id").get();
        
        assertThat(updatedShow).hasFieldOrPropertyWithValue("shouldGetRatingChecked", true);
                    
}

    @Test
    public void setIsRecommended_shouldUpdateIsRecommended(){
        Show show = new Show();
        show.setTraktId("id");
        showRepository.save(show);

        showRepository.setIsRecommended(true, "id");
        Show updatedShow = showRepository.findById("id").get();
        
        assertThat(updatedShow).hasFieldOrPropertyWithValue("isRecommended", true);
                    
}
}
