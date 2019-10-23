package tadeas_musil.tv_series_tracker.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import tadeas_musil.tv_series_tracker.model.SearchResult;
import tadeas_musil.tv_series_tracker.service.ShowService;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ShowsController.class, secure = false)
public class ShowControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ShowService showService;
    
    @Before
    public void setUp() throws Exception{
        SearchResult searchResult = new SearchResult();
        searchResult.setTotalNumberOfPages(5);
        searchResult.setShows(new ArrayList<>());
        when(showService.searchShows("query", 1)).thenReturn(searchResult);
    }

    @Test
     public void shouldReturnSearchPage() throws Exception{
        mockMvc.perform(get("/shows/search")
                .param("query", "query")
                .param("page", "1"))
        .andExpect(model().attributeExists("shows"))
        .andExpect(model().attributeExists("previousPage"))
        .andExpect(model().attributeExists("currentPage"))
        .andExpect(model().attributeExists("nextPage"))
        .andExpect(model().attributeExists("totalNumberOfPages"))
        .andExpect(model().attributeExists("showToTrack"))
        .andExpect(status().isOk())
        .andExpect(view().name("shows-search"));
    }
}