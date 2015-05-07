package alexp.blog.service;

import alexp.blog.controller.ForbiddenException;
import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service("commentService")
public class CommentServiceImpl implements CommentService {

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public List<Comment> getPostComments(Post post) {
        return post.getComments();
    }

    @Override
    public Comment getComment(Long id) {
        return commentRepository.findOne(id);
    }

    @Override
    public void saveNewComment(Comment comment, Post post) {
        comment.setDateTime(LocalDateTime.now());

        comment.setPost(post);

        comment.setUser(userService.currentUser());

        commentRepository.saveAndFlush(comment);
    }

    @Override
    public void deleteComment(Long commentId) throws ActionExpiredException {
        Comment comment = getComment(commentId);

        boolean isAdmin = userService.isAdmin();

        if (!isAdmin && !userService.currentUser().getUsername().equals(comment.getUser().getUsername())) {
            throw new ForbiddenException();
        }

        if (!isAdmin && !comment.userCanDelete()) {
            throw new ActionExpiredException("delete time exceeded");
        }

        comment.setDeleted(true);

        commentRepository.saveAndFlush(comment);
    }
}
