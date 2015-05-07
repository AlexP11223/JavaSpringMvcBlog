package alexp.blog.service;

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
    public void saveNewComment(Comment comment, Post post) {
        comment.setDateTime(LocalDateTime.now());

        comment.setPost(post);

        comment.setUser(userService.currentUser());

        commentRepository.saveAndFlush(comment);
    }
}
