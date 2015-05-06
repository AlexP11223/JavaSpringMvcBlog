package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import alexp.blog.model.User;
import alexp.blog.service.UserServiceImpl;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static alexp.blog.utils.SecurityUtils.userBob;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DatabaseSetup("data.xml")

public class UsersControllerIT extends AbstractIntegrationTest {

    @Autowired
    UserServiceImpl userService;

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

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowSettingsPage() throws Exception {
        mockMvc.perform(get("/settings").with(userBob()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attribute("user", hasProperty("id", equalTo(3L))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldNotShowSettingsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isFound())
                .andExpect(view().name(is(not(equalTo("settings")))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenySettingsChangeWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/change_email").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", ""))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(post("/change_password").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", ""))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnSettingsFormWithErrorsWhenSubmittedInvalidEmailChangeInput() throws Exception {
        mockMvc.perform(post("/change_email").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", "")
                .param("email", "notemail"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeHasFieldErrors("user", "email"));

        String email = "newemail@gmail.com";

        mockMvc.perform(post("/change_email").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", "incorrect")
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "NotMatchCurrent"));
    }

    @Test
    @ExpectedDatabase("data-email-changed.xml")
    public void shouldChangeEmail() throws Exception {
        String email = "newemail@gmail.com";

        mockMvc.perform(post("/change_email").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", "pass123")
                .param("email", email))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/settings"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnSettingsFormWithErrorsWhenSubmittedInvalidPasswordChangeInput() throws Exception {
        mockMvc.perform(post("/change_password").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeHasFieldErrors("user", "password"));

        mockMvc.perform(post("/change_password").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", "incorrect")
                .param("password", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "NotMatchCurrent"));
    }

    @Test
    @ExpectedDatabase("data-password-changed.xml")
    @DirtiesContext
    public void shouldChangePassword() throws Exception {
        String currentPassword = "pass123";
        String currentPasswordHash = "$2a$10$TB9YW0AYPqF1PSfMKrANAuvxLFZSYc2b7uJv0c3wZ8BM45zs1MBLO";
        String password = "newPassword";

        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(passwordEncoder.matches(currentPassword, currentPasswordHash)).thenReturn(true);
        userService.setPasswordEncoder(passwordEncoder);

        mockMvc.perform(post("/change_password").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", currentPassword)
                .param("password", password))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/settings"));

        verify(passwordEncoder, times(1)).encode(password);
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowEditProfilePage() throws Exception {
        mockMvc.perform(get("/edit_profile").with(userBob()))
                .andExpect(status().isOk())
                .andExpect(view().name("editprofile"))
                .andExpect(model().attribute("user", hasProperty("id", equalTo(3L))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldNotShowEditProfilePageWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/edit_profile"))
                .andExpect(status().isFound())
                .andExpect(view().name(is(not(equalTo("editprofile")))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnEditProfileFormWithErrorsWhenSubmittedInvalidInput() throws Exception {
        String url = "notlink";

        mockMvc.perform(post("/edit_profile").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("websiteLink", url))
                .andExpect(status().isOk())
                .andExpect(view().name("editprofile"))
                .andExpect(model().attributeHasFieldErrorCode("user", "websiteLink", "Pattern"))
                .andExpect(model().errorCount(1));
    }

    @Test
    @ExpectedDatabase("data-profile-changed.xml")
    public void shouldEditProfile() throws Exception {
        String url = "http://site.com";
        String about = "new about";

        mockMvc.perform(post("/edit_profile").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("websiteLink", url)
                .param("aboutText", about))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/edit_profile"));
    }

    @Test
    @DatabaseSetup("data-profile-changed.xml")
    @ExpectedDatabase("data.xml")
    public void shouldResetProfileInfo() throws Exception {
        mockMvc.perform(post("/edit_profile").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/edit_profile"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowProfilePage() throws Exception {
        mockMvc.perform(get("/users/Bob").with(userBob()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", hasProperty("id", equalTo(3L))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturn404IfUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/notexisting").with(userBob()))
                .andExpect(status().isNotFound());
    }
}