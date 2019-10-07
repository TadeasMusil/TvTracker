package tadeas_musil.tv_series_tracker.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
   
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    private User joe;
    
    @Before
    public void setUp(){
        joe = new User();
        joe.setUsername("joe@email.com");
        joe.setPassword("password");
        joe.setDailyScheduleNotification(false);
        joe.setNewShowNotification(false);
        when(userService.getByUsername(any())).thenReturn(joe);
    }
    @Test
    public void showUserSettingsShouldGetRedirectedWithoutAuthentication() throws Exception{
        mockMvc.perform(get("/user/settings"))
        .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    public void shouldReturnSettingsPage() throws Exception{
        mockMvc.perform(get("/user/settings"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("user"))
        .andExpect(model().attribute("user",hasProperty("username", is("joe@email.com"))))
        .andExpect(model().attribute("user",hasProperty("password", is("password"))))
        .andExpect(model().attribute("user",hasProperty("dailyScheduleNotification", is(false))))
        .andExpect(model().attribute("user",hasProperty("newShowNotification", is(false))))
        .andExpect(view().name("settings"))
        ;
    }

    @Test
    public void shouldReturnStatusForbidden() throws Exception{
        mockMvc.perform(post("/user/updateUser"))
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void givenTooShortPassword_shouldHaveError() throws Exception{
        mockMvc.perform(post("/user/updateUser")
        .param("password", "pass")
        .param("password", "pass")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("user"))
        .andExpect(model().attributeHasFieldErrors("user", "password"))
    	.andExpect(view().name("settings"));
    }

    @Test
    @WithMockUser
    public void givenNoPassword_shouldBeWithoutErrors() throws Exception{
        mockMvc.perform(post("/user/updateUser")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().hasNoErrors())
    	.andExpect(view().name("settings"));
    }
    @Test
    @WithMockUser
    public void followingShow_ShouldGetRedirected() throws Exception{
        mockMvc.perform(post("/user/followShow")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());
    }
    @Test
    @WithMockUser
    public void unfollowingShow_ShouldGetRedirected() throws Exception{
        mockMvc.perform(post("/user/unfollowShow")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    public void shouldReturnUserShows() throws Exception{
        when(userService.getByUsernameWithShows(any())).thenReturn(joe);
        mockMvc.perform(post("/user/shows")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("shows"))
    	.andExpect(view().name("user-shows"));
    }
}
