package alexp.blog.model;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;

public class PostTest {

    private Post post;

    @Before
    public void setUp() {
        post = new Post();
    }

    @Test
    public void shouldGetTopLevelComments() {
        Comment comment = new Comment();
        Comment replyComment = new Comment();
        replyComment.setParentComment(comment);
        comment.setChildrenComments(Arrays.asList(new Comment(), comment));

        post.setComments(Arrays.asList(new Comment(), comment));

        List<Comment> comments = post.topLevelComments();

        assertThat(comments.size(), equalTo(2));

        assertThat(comments, not(hasItem(replyComment)));
    }

    @Test
    public void shouldReturnEmptyList() {
        List<Comment> comments = post.topLevelComments();

        assertThat(comments.size(), equalTo(0));
    }

    @Test
    public void testRatingSum() {
        assertThat(post.getRatingSum(), equalTo(0));

        post.getPostRatings().add(new PostRating(null, Rating.LIKE_VALUE, null));

        assertThat(post.getRatingSum(), equalTo(1));

        post.getPostRatings().add(new PostRating(null, Rating.DISLIKE_VALUE, null));
        post.getPostRatings().add(new PostRating(null, Rating.DISLIKE_VALUE, null));

        assertThat(post.getRatingSum(), equalTo(-1));
    }

    @Test
    public void testUserVoteValue() {
        assertThat(post.getUserVoteValue(null), equalTo((short) 0));
        assertThat(post.getUserVoteValue(1L), equalTo((short) 0));

        User user = new User();
        user.setId(1L);

        post.getPostRatings().add(new PostRating(user, Rating.LIKE_VALUE, null));

        assertThat(post.getUserVoteValue(user.getId()), equalTo(Rating.LIKE_VALUE));
    }
}