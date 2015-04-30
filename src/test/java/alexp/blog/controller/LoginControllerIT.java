package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import org.junit.Test;
import org.springframework.http.MediaType;

import static alexp.blog.utils.SecurityUtils.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DatabaseSetup("data.xml")
public class LoginControllerIT extends AbstractIntegrationTest {

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldNotAuthenticateWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/login_check").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "Bob")
                .param("password", "invalid_password"))
                .andExpect(status().isFound())
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login_error"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldAuthenticateByUsername() throws Exception {
        mockMvc.perform(post("/login_check").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "Bob")
                .param("password", "pass123"))
                .andExpect(status().isFound())
                .andExpect(authenticated().withUsername("Bob"))
                .andExpect(redirectedUrl("/login_success"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldAuthenticateByEmail() throws Exception {
        mockMvc.perform(post("/login_check").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "bob@gmail.com")
                .param("password", "pass123"))
                .andExpect(status().isFound())
                .andExpect(authenticated().withUsername("Bob"))
                .andExpect(redirectedUrl("/login_success"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldLogout() throws Exception {
        mockMvc.perform(post("/dologout").with(csrf()).with(userBob()))
                .andExpect(status().isFound())
                .andExpect(unauthenticated());
    }

}