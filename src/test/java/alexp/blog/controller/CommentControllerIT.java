package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import alexp.blog.utils.HsqldbSequenceResetter;
import com.github.springtestdbunit.annotation.*;
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
@DbUnitConfiguration(databaseOperationLookup = HsqldbSequenceResetter.class)
public class CommentControllerIT extends AbstractIntegrationTest {

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowComments() throws Exception {
        mockMvc.perform(get("/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/comments :: commentList"))
                .andExpect(model().attribute("comments", hasSize(2)))
                .andExpect(model().attribute("comments",
                        allOf(hasItem(hasProperty("childrenComments", hasSize(1))),
                                hasItem(hasProperty("childrenComments", hasSize(0))))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturn404WhenPostNotExists() throws Exception {
        mockMvc.perform(get("/posts/999/comments"))
                .andExpect(status().isNotFound());
    }

    @Test
    @ExpectedDatabase("data-post-hidden.xml")
    @DatabaseSetup("data-post-hidden.xml")
    public void shouldReturn404WhenPostIsHidden() throws Exception {
        mockMvc.perform(get("/posts/1/comments"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/posts/1/comments").with(userBob()))
                .andExpect(status().isNotFound());
    }

    @Test
    @ExpectedDatabase("data-post-hidden.xml")
    @DatabaseSetup("data-post-hidden.xml")
    public void shouldShowCommentsWhenPostIsHiddenAndAdmin() throws Exception {
        mockMvc.perform(get("/posts/1/comments").with(userAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldGetCommentSource() throws Exception {
        mockMvc.perform(get("/posts/1/comments/3/source").with(userBob()))
                .andExpect(status().isOk())
                .andExpect(content().string("comment3 text"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyCommentIfNotAuthorized() throws Exception {
        mockMvc.perform(post("/posts/1/comments/create").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnErrorWhenSubmittedInvalidComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/create").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("error")));

        mockMvc.perform(post("/posts/999/comments/create").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", "text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @ExpectedDatabase(value = "data-comment-added.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value = "data.xml", type = DatabaseOperation.TRUNCATE_TABLE) // to reset id sequence, otherwise other tests that insert comments will fail on ExpectedDatabase
    public void shouldAddComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/create").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", "new comment text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")));
    }

    @Test
    @ExpectedDatabase(value = "data-comment-reply-added.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value = "data.xml", type = DatabaseOperation.TRUNCATE_TABLE) // to reset id sequence, otherwise other tests that insert comments will fail on ExpectedDatabase
    public void shouldAddReplyComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/create").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", "new comment text")
                .param("parentId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")));
    }

    @Test
    @ExpectedDatabase(value = "data-comment-reply-added-limit-exceeded.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value = "data.xml", type = DatabaseOperation.TRUNCATE_TABLE) // to reset id sequence, otherwise other tests that insert comments will fail on ExpectedDatabase
    public void shouldAddReplyToPreviousLevelWhenLimitExceeded() throws Exception {
        for (int i = 3; i <= 9; i++) {
            mockMvc.perform(post("/posts/1/comments/create").with(userBob()).with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("commentText", "new comment text")
                    .param("parentId", Integer.toString(i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ok")));
        }
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyDeleteOtherUserComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/3/delete").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyDeleteCommentIfExpired() throws Exception {
        mockMvc.perform(post("/posts/1/comments/3/delete").with(userAlice()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("expired"));
    }

    @Test
    @ExpectedDatabase(value = "data-comment-added-and-marked-deleted.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value = "data.xml", type = DatabaseOperation.TRUNCATE_TABLE) // to reset id sequence, otherwise other tests that insert comments will fail on ExpectedDatabase
    public void shouldDeleteComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/create").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", "new comment text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")));

        mockMvc.perform(post("/posts/1/comments/4/delete").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @ExpectedDatabase("data-comment-marked-deleted.xml")
    public void shouldAllowDeleteAnyCommentIfAdmin() throws Exception {
        mockMvc.perform(post("/posts/1/comments/3/delete").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyEditOtherUserComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/3/edit").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", "new comment text"))
                .andExpect(status().isForbidden());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyEditCommentIfExpired() throws Exception {
        mockMvc.perform(post("/posts/1/comments/3/edit").with(userAlice()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", "new comment text"))
                .andExpect(status().isOk())
                .andExpect(content().string("expired"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnErrorWhenSubmittedInvalidEditComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/3/edit").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(not(equalTo("ok"))));
    }

    @Test
    @ExpectedDatabase(value = "data-comment-added-and-edited.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value = "data.xml", type = DatabaseOperation.TRUNCATE_TABLE) // to reset id sequence, otherwise other tests that insert comments will fail on ExpectedDatabase
    public void shouldEditComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments/create").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", "new comment text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")));

        String text = "edited text";

        mockMvc.perform(post("/posts/1/comments/4/edit").with(userBob()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", text))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @ExpectedDatabase("data-comment-edited.xml")
    public void shouldAllowEditedAnyCommentIfAdmin() throws Exception {
        String text = "edited text";

        mockMvc.perform(post("/posts/1/comments/3/edit").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("commentText", text))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}