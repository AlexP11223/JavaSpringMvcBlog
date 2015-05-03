package alexp.blog.service;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.model.User;
import alexp.blog.repository.CommentRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CommentServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Spy
    private Post post;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetComments() {
        post.setComments(Arrays.asList(new Comment(), new Comment()));

        List<Comment> comments = commentService.getPostComments(post);

        assertThat(comments.size(), is(equalTo(2)));

        verify(post, times(1)).getComments();
    }

    @Test
    public void shouldReturnNoComments() {
        List<Comment> comments = commentService.getPostComments(post);

        assertThat(comments.size(), is(equalTo(0)));

        verify(post, times(1)).getComments();
    }

    @Test
    public void shouldAddNewComment() {
        User user = new User();

        when(userService.currentUser()).thenReturn(user);

        Comment comment = new Comment();

        commentService.saveNewComment(comment, post);

        assertThat(comment.getPost(), is(equalTo(post)));

        assertThat(comment.getUser(), is(equalTo(user)));

        verify(commentRepository, times(1)).saveAndFlush(Matchers.any(Comment.class));
    }
}