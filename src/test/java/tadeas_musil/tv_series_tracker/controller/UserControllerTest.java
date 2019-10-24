package tadeas_musil.tv_series_tracker.controller;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

import tadeas_musil.tv_series_tracker.model.Show;
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
    

    
    @Before
    public void setUp(){
        
    
    }

    private User getTestUser(){
        User user = new User();
        user.setUsername("test@email.com");
        user.setPassword("password");
        user.setGettingRecommendedShowsNotification(false);
        user.setGettingScheduleNotification(false);
        user.getFollowedShows().add(new Show());
        return user;
    }
    @Test
    public void userSettings_ShouldGetRedirected_WithoutAuthentication() throws Exception{
        mockMvc.perform(get("/user/settings"))
        .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    public void userSettings_shouldReturnSettingsWithUserInfo_WhenAuthenticated() throws Exception{
        User user = getTestUser();
        when(userService.getByUsername(any())).thenReturn(user);
        
        mockMvc.perform(get("/user/settings"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("user"))
        .andExpect(model().attribute("user",hasProperty("username", is("test@email.com"))))
        .andExpect(model().attribute("user",hasProperty("password", is("password"))))
        .andExpect(model().attribute("user",hasProperty("gettingScheduleNotification", is(false))))
        .andExpect(model().attribute("user",hasProperty("gettingRecommendedShowsNotification", is(false))))
        .andExpect(view().name("settings"));
    }

    @Test
    public void updateUser_shouldReturnStatusForbidden_withoutAuthentication() throws Exception{
        mockMvc.perform(post("/user/updateUser"))
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void updateUser_shouldHaveError_givenTooShortPassword() throws Exception{
        mockMvc.perform(post("/user/updateUser")
        .param("password", "pass")
        .param("confirmPassword", "pass")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("user"))
        .andExpect(model().attributeHasFieldErrors("user", "password"))
    	.andExpect(view().name("settings"));
    }

    @Test
    @WithMockUser
    public void updateUser_shouldBeWithoutErrors_givenNoPassword() throws Exception{
        User user = getTestUser();
        when(userService.getByUsername(any())).thenReturn(user);
        
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
    public void userShows_shouldReturnUsersShows() throws Exception{
        User user = getTestUser();
        when(userService.getByUsernameWithShows(any())).thenReturn(user);
        
        mockMvc.perform(post("/user/shows")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("shows"))
        .andExpect(model().attribute("shows", user.getFollowedShows()))
    	.andExpect(view().name("user-shows"));
    }
}
