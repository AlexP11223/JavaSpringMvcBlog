package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import static alexp.blog.utils.SecurityUtils.userAdmin;
import static alexp.blog.utils.SecurityUtils.userBob;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DatabaseSetup("data.xml")
public class PostsControllerIT extends AbstractIntegrationTest {

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowPostsPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowPostPage() throws Exception {
        mockMvc.perform(get("/post?id=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attributeDoesNotExist("comment"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowCommentFormWhenAuthorized() throws Exception {
        mockMvc.perform(get("/post?id=1").with(userBob()))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attribute("comment", instanceOf(Comment.class)));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturn404WhenPostNotExists() throws Exception {
        mockMvc.perform(get("/post?id=999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowCreatePostPageIfAdmin() throws Exception {
        mockMvc.perform(get("/post/create").with(userAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("createpost"))
                .andExpect(model().attribute("post", instanceOf(Post.class)));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyCreatePostIfNotAdmin() throws Exception {
        mockMvc.perform(get("/post/create"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/post/create").with(userBob()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/post/create").with(userBob()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnPostFormWithErrorsWhenSubmittedInvalidPost() throws Exception {
        mockMvc.perform(post("/post/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("post", "title"))
                .andExpect(model().attributeHasFieldErrors("post", "fullPostText"));

        String title = "post title";
        String text = "too short";

        mockMvc.perform(post("/post/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("fullPostText", text))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("post", "fullPostText"))
                .andExpect(model().attribute("post", hasProperty("title", Matchers.is(equalTo(title)))))
                .andExpect(model().attribute("post", hasProperty("fullPostText", Matchers.is(equalTo(text)))));
    }

    @Test
    @ExpectedDatabase(value = "data-posts-added.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void shouldAddPosts() throws Exception {
        String title = "new post title";
        String text = "new post short text===cut===new post full text Lorem ipsum";
        String tags = "c++, java, hello world";

        mockMvc.perform(post("/post/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("fullPostText", text)
                .param("tagstext", tags))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("redirect:/posts"));

        String text2 = "new post 2 text Lorem ipsum dolor sit amet, consectetur adipiscing elit";
        String tags2 = "java";

        mockMvc.perform(post("/post/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("fullPostText", text2)
                .param("tagstext", tags2))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("redirect:/posts"));
    }
}