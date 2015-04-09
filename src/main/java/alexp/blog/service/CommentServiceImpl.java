package alexp.blog.service;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.model.Tag;
import alexp.blog.repository.CommentRepository;
import alexp.blog.repository.PostRepository;
import alexp.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
        comment.setDateTime(new Date());

        comment.setPost(post);

        comment.setUser(userService.currentUser());

        commentRepository.save(comment);
    }
}
