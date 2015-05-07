package alexp.blog.service;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.model.User;
import alexp.blog.repository.CommentRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

        assertThat(comment.getDateTime().toLocalDate().equals(LocalDate.now()), is(equalTo(true)));

        verify(commentRepository, times(1)).saveAndFlush(Matchers.any(Comment.class));
    }

    @Test
    public void shouldGetComment() {
        final long commentId = 1L;

        Comment comment = new Comment();

        when(commentRepository.findOne(commentId)).thenReturn(comment);

        Comment retrievedComment = commentService.getComment(commentId);

        assertThat(retrievedComment, is(equalTo(comment)));

        verify(commentRepository, times(1)).findOne(commentId);
    }

    @Test
    public void shouldReturnNullWhenCommentDoesNotExist() {
        when(commentRepository.findOne(Matchers.anyLong())).thenReturn(null);

        assertThat(commentService.getComment(1L), is(equalTo(null)));

        verify(commentRepository, times(1)).findOne(Matchers.anyLong());
    }
}