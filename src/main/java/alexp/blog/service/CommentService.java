package alexp.blog.service;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {

    List<Comment> getPostComments(Post post);

    void saveNewComment(Comment comment, Post post);
}
