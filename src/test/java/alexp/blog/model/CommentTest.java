package alexp.blog.model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class CommentTest {

    private Comment comment;

    @Before
    public void setUp() {
        comment = new Comment();
    }

    @Test
    public void commentLevel() {
        assertThat(comment.commentLevel(), equalTo(0));

        Comment replyComment = new Comment();
        replyComment.setParentComment(comment);

        assertThat(replyComment.commentLevel(), equalTo(1));

        Comment replyComment2 = new Comment();
        replyComment2.setParentComment(replyComment);

        assertThat(replyComment2.commentLevel(), equalTo(2));
    }

    @Test
    public void testRatingSum() {
        assertThat(comment.getRatingSum(), equalTo(0));

        comment.getCommentRatings().add(new CommentRating(null, Rating.LIKE_VALUE, null));

        assertThat(comment.getRatingSum(), equalTo(1));

        comment.getCommentRatings().add(new CommentRating(null, Rating.DISLIKE_VALUE, null));
        comment.getCommentRatings().add(new CommentRating(null, Rating.DISLIKE_VALUE, null));

        assertThat(comment.getRatingSum(), equalTo(-1));
    }

    @Test
    public void testUserVoteValue() {
        assertThat(comment.getUserVoteValue(null), equalTo((short) 0));
        assertThat(comment.getUserVoteValue(1L), equalTo((short) 0));

        User user = new User();
        user.setId(1L);

        comment.getCommentRatings().add(new CommentRating(user, Rating.LIKE_VALUE, null));

        assertThat(comment.getUserVoteValue(user.getId()), equalTo(Rating.LIKE_VALUE));
    }
}