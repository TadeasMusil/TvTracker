package tadeas_musil.tv_series_tracker.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    UserService userService;
    
    @Test
    public void shouldReturnRegistrationPage() throws Exception {
    	mockMvc.perform(get("/registration"))
    	.andExpect(status().isOk())
    	.andExpect(model().attributeExists("user"))
    	.andExpect(view().name("registration-form"));
    }

    @Test
    public void shouldReturnIndexPage() throws Exception {
        mockMvc.perform(post("/processRegistration")
            .param("username", "joe@email.com")
            .param("password", "password")
            .param("confirmPassword", "password")
            .with(csrf()))
        .andExpect(status().isOk())
    	.andExpect(view().name("index"));
    }
    
    @Test 
    public void givenNonMatchingPasswords_shouldHaveError() throws Exception {
        mockMvc.perform(post("/processRegistration")
            .param("username", "joe@email.com")
            .param("password", "password")
            .param("confirmPassword", "differentPassword")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("user"))
        .andExpect(model().attributeHasFieldErrors("user", "password"))
    	.andExpect(view().name("registration-form"));
    }

    @Test 
    public void registeringExistingUser_shouldHaveError() throws Exception{
        when(userService.getByUsername("joe@email.com")).thenReturn(new User());
        
        mockMvc.perform(post("/processRegistration")
            .param("username", "joe@email.com")
            .param("password", "password")
            .param("confirmPassword", "password")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("user"))
        .andExpect(model().attributeHasFieldErrors("user", "username"))
    	.andExpect(view().name("registration-form"));
    }
}
