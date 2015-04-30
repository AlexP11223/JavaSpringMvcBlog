package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.junit.Test;
import org.springframework.http.MediaType;

import static alexp.blog.utils.SecurityUtils.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DatabaseSetup("data.xml")
public class CommentControllerIT extends AbstractIntegrationTest {

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowComments() throws Exception {
        mockMvc.perform(get("/comments/show?postid=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/comments :: commentList"))
                .andExpect(model().attribute("comments", hasSize(3)));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturn404WhenPostNotExists() throws Exception {
        mockMvc.perform(get("/comments/show?postid=999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyCommentIfNotAuthorized() throws Exception {
        mockMvc.perform(post("/comment/add").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("postid", "1"))
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnErrorTextWhenSubmittedInvalidComment() throws Exception {
        mockMvc.perform(post("/comment/add").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("postid", "1")
                .param("commentText", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(not(equalTo("ok"))));

        mockMvc.perform(post("/comment/add").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("postid", "999")
                .param("commentText", "text"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(equalTo("ok"))));
    }

    @Test
    @ExpectedDatabase(value = "data-comment-added.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void shouldAdddComment() throws Exception {
        mockMvc.perform(post("/comment/add").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("postid", "1")
                .param("commentText", "new comment text"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}