package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import alexp.blog.model.User;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import static alexp.blog.utils.SecurityUtils.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DatabaseSetup("data.xml")

public class RegistrationControllerIT extends AbstractIntegrationTest {

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldFindByUsername() throws Exception {
        mockMvc.perform(get("/check_username")
                .param("username", "bob"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldNotFindByNonExistingUsername() throws Exception {
        mockMvc.perform(get("/check_username")
                .param("username", "notexists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldFindByEmail() throws Exception {
        mockMvc.perform(get("/check_email")
                .param("email", "bob@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldNotFindByNonExistingEmail() throws Exception {
        mockMvc.perform(get("/check_email")
                .param("email", "notexists@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("user", instanceOf(User.class)));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldRedirectFromRegistrationPageIfAuthorized() throws Exception {
        mockMvc.perform(get("/register").with(userBob()))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:posts"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnRegistrationFormWithErrorsWhenSubmittedInvalidUser() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("user", "username"))
                .andExpect(model().attributeHasFieldErrors("user", "email"))
                .andExpect(model().attributeHasFieldErrors("user", "password"));

        String username = "newuser";
        String email = "newemail@gmail.com";
        String password = "pass123";

        mockMvc.perform(post("/register").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "bob")
                .param("email", email)
                .param("password", password))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("user", "username"))
                .andExpect(model().attribute("user", hasProperty("email", Matchers.is(equalTo(email)))));

        mockMvc.perform(post("/register").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("email", "bob@gmail.com")
                .param("password", password))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("user", "email"))
                .andExpect(model().attribute("user", hasProperty("username", Matchers.is(equalTo(username)))));
    }

    @Test
    @ExpectedDatabase(value = "data-user-added.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void shouldRegisterUser() throws Exception {
        String username = "newuser";
        String email = "newemail@gmail.com";
        String password = "pass123";

        mockMvc.perform(post("/register").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("email", email)
                .param("password", password))
                .andExpect(status().isFound());
    }
}