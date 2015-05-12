package alexp.blog.model;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class CommentTest {

    @Test
    public void commentLevel() {
        Comment comment = new Comment();

        assertThat(comment.commentLevel(), equalTo(0));

        Comment replyComment = new Comment();
        replyComment.setParentComment(comment);

        assertThat(replyComment.commentLevel(), equalTo(1));

        Comment replyComment2 = new Comment();
        replyComment2.setParentComment(replyComment);

        assertThat(replyComment2.commentLevel(), equalTo(2));
    }
}