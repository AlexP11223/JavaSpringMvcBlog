package alexp.blog.service;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;

import java.util.List;

public interface CommentService {

    List<Comment> getPostComments(Post post);

    Comment getComment(Long id);

    void saveNewComment(Comment comment, Post post);

    void deleteComment(Long commentId) throws ActionExpiredException;
}
