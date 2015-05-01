package alexp.blog.controller;

import alexp.blog.AbstractIntegrationTest;
import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.model.PostEditDto;
import alexp.blog.utils.HsqldbSequenceResetter;
import com.github.springtestdbunit.annotation.*;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import static alexp.blog.utils.SecurityUtils.userAdmin;
import static alexp.blog.utils.SecurityUtils.userBob;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DatabaseSetup("data.xml")
@DbUnitConfiguration(databaseOperationLookup = HsqldbSequenceResetter.class)
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
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attributeDoesNotExist("comment"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowCommentFormWhenAuthorized() throws Exception {
        mockMvc.perform(get("/posts/1").with(userBob()))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attribute("comment", instanceOf(Comment.class)));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturn404WhenPostNotExists() throws Exception {
        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowCreatePostPageIfAdmin() throws Exception {
        mockMvc.perform(get("/posts/create").with(userAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("editpost"))
                .andExpect(model().attribute("post", instanceOf(PostEditDto.class)))
                .andExpect(model().attribute("edit", is(equalTo(false))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyCreatePostIfNotAdmin() throws Exception {
        mockMvc.perform(get("/posts/create"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/posts/create").with(userBob()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/posts/create").with(userBob()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnAddPostFormWithErrorsWhenSubmittedInvalidPost() throws Exception {
        mockMvc.perform(post("/posts/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("post", "title"))
                .andExpect(model().attributeHasFieldErrors("post", "text"))
                .andExpect(model().attribute("edit", is(equalTo(false))));

        String title = "post title";
        String text = "too short";

        mockMvc.perform(post("/posts/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("text", text))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("post", "text"))
                .andExpect(model().attribute("post", hasProperty("title", Matchers.is(equalTo(title)))))
                .andExpect(model().attribute("post", hasProperty("text", Matchers.is(equalTo(text)))))
                .andExpect(model().attribute("edit", is(equalTo(false))));
    }

    @Test
    @ExpectedDatabase(value = "data-posts-added.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    @DatabaseTearDown(value = "data.xml", type = DatabaseOperation.TRUNCATE_TABLE) // to reset id sequence, otherwise other tests that insert tags will fail on ExpectedDatabase
    public void shouldAddPosts() throws Exception {
        String title = "new post title";
        String text = "new post short text===cut===new post full text Lorem ipsum";
        String tags = "c++, java, hello world";

        mockMvc.perform(post("/posts/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("text", text)
                .param("tags", tags))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("redirect:/posts"));

        String text2 = "new post 2 text Lorem ipsum dolor sit amet, consectetur adipiscing elit";
        String tags2 = "java";

        mockMvc.perform(post("/posts/create").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("text", text2)
                .param("tags", tags2))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("redirect:/posts"));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldShowEditPostPageIfAdmin() throws Exception {
        mockMvc.perform(get("/posts/1/edit").with(userAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("editpost"))
                .andExpect(model().attribute("post", hasProperty("id", is(Matchers.equalTo(1L)))))
                .andExpect(model().attribute("edit", is(equalTo(true))));
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldDenyEditPostIfNotAdmin() throws Exception {
        mockMvc.perform(get("/posts/1/edit"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/posts/1/edit").with(userBob()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/posts/1/edit").with(userBob()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @ExpectedDatabase("data.xml")
    public void shouldReturnEditPostFormWithErrorsWhenSubmittedInvalidPost() throws Exception {
        mockMvc.perform(post("/posts/1/edit").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("post", "title"))
                .andExpect(model().attributeHasFieldErrors("post", "text"))
                .andExpect(model().attribute("post", hasProperty("id", is(Matchers.equalTo(1L)))))
                .andExpect(model().attribute("edit", is(equalTo(true))));

        String title = "post title";
        String text = "too short";

        mockMvc.perform(post("/posts/1/edit").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("text", text))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("post", "text"))
                .andExpect(model().attribute("post", hasProperty("title", Matchers.is(equalTo(title)))))
                .andExpect(model().attribute("post", hasProperty("text", Matchers.is(equalTo(text)))))
                .andExpect(model().attribute("post", hasProperty("id", is(Matchers.equalTo(1L)))))
                .andExpect(model().attribute("edit", is(equalTo(true))));
    }

    @Test
    @ExpectedDatabase(value = "data-post-edited.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    @DatabaseTearDown(value = "data.xml", type = DatabaseOperation.TRUNCATE_TABLE) // to reset id sequence, otherwise other tests that insert tags will fail on ExpectedDatabase
    public void shouldEditPosts() throws Exception {
        String title = "edited title";
        String text = "edited Lorem ipsum dolor sit amet, consectetur adipiscing elit";
        String tags = "c, c++, c#";

        mockMvc.perform(post("/posts/1/edit").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("text", text)
                .param("tags", tags))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("redirect:/posts/1"));

        String text2 = "edited short===cut===edited Lorem ipsum dolor sit amet, consectetur adipiscing elit";
        String tags2 = "c++, meow";

        mockMvc.perform(post("/posts/2/edit").with(userAdmin()).with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", title)
                .param("text", text2)
                .param("tags", tags2))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("redirect:/posts/2"));
    }
}