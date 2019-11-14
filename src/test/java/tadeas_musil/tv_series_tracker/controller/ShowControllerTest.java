package tadeas_musil.tv_series_tracker.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import tadeas_musil.tv_series_tracker.model.ShowsPage;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.service.ShowService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ShowControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ShowService showService;

    @Test
     public void shouldReturnSearchPage() throws Exception{
        List<Show> shows = new ArrayList<>();
        ShowsPage page = new ShowsPage();
        page.setTotalNumberOfPages(5);
        page.setShows(shows);
        when(showService.searchShows("query", 1)).thenReturn(page);

        mockMvc.perform(get("/shows/search")
                .param("query", "query")
                .param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("shows"))
        .andExpect(model().attributeExists("previousPage"))
        .andExpect(model().attributeExists("currentPage"))
        .andExpect(model().attributeExists("nextPage"))
        .andExpect(model().attributeExists("showToTrack"))
        .andExpect(model().attribute("totalNumberOfPages", 5))
        .andExpect(model().attribute("shows", shows))
        .andExpect(view().name("shows-search"));
    }

    @Test
     public void shouldReturnRecommendedShowsPage() throws Exception{
        List<Show> shows = List.of(new Show(), new Show());
        Page<Show> page = new PageImpl<Show>(shows, PageRequest.of(0, 1), 2);
        when(showService.getRecommendedShows(anyInt())).thenReturn(page);

        mockMvc.perform(get("/shows/recommended")
                .param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("shows"))
        .andExpect(model().attributeExists("previousPage"))
        .andExpect(model().attributeExists("currentPage"))
        .andExpect(model().attributeExists("nextPage"))
        .andExpect(model().attributeExists("showToTrack"))
        .andExpect(model().attribute("totalNumberOfPages", 2))
        .andExpect(model().attribute("shows", shows))
        .andExpect(view().name("recommended-shows"));
    }
}
