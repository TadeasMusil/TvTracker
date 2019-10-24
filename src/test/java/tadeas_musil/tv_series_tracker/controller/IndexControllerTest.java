package tadeas_musil.tv_series_tracker.controller;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = IndexController.class, secure = false)
public class IndexControllerTest {
    
    @Autowired
    private MockMvc mvc;
    
    @Test
    public void shouldReturnIndexPage() throws Exception{
        mvc.perform(get("/"))
        	.andExpect(status().isOk())
            .andExpect(view().name("index"));
    }

}
