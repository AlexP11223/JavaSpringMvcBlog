package alexp.blog.service;

import alexp.blog.controller.ForbiddenException;
import alexp.blog.model.*;
import alexp.blog.repository.CommentRatingRepository;
import alexp.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("commentService")
public class CommentServiceImpl implements CommentService {

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentRatingRepository commentRatingRepository;

    private static final int MAX_COMMENT_LEVEL = 5;

    @Override
    public Comment getComment(Long id) {
        return commentRepository.findOne(id);
    }

    @Override
    public Long saveNewComment(Comment comment, Post post, Long parentId) {
        if (parentId != null) {
            Comment parentComment = getComment(parentId);

            int level = parentComment.commentLevel();

            comment.setParentComment(level < MAX_COMMENT_LEVEL ? parentComment : parentComment.getParentComment());
        }

        comment.setDateTime(LocalDateTime.now());

        comment.setPost(post);

        comment.setUser(userService.currentUser());

        commentRepository.saveAndFlush(comment);

        return comment.getId();
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

    @Override
    public void updateComment(Comment newCommentData, Long commentId) throws ActionExpiredException {
        Comment comment = getComment(commentId);

        boolean isAdmin = userService.isAdmin();

        if (!isAdmin && !userService.currentUser().getUsername().equals(comment.getUser().getUsername())) {
            throw new ForbiddenException();
        }

        if (!isAdmin && !comment.userCanEdit()) {
            throw new ActionExpiredException("edit time exceeded");
        }

        comment.setCommentText(newCommentData.getCommentText());

        comment.setModifiedDateTime(LocalDateTime.now());

        commentRepository.saveAndFlush(comment);
    }

    @Override
    public void vote(Long commentId, boolean like) throws AlreadyVotedException, ForbiddenException {
        User currentUser = userService.currentUser();

        Comment comment = getComment(commentId);

        if (currentUser.getId().longValue() == comment.getUser().getId().longValue()) {
            throw new ForbiddenException("cannot vote for own comments");
        }

        CommentRating rating = commentRatingRepository.findUserRating(commentId, currentUser.getId());

        if (rating != null) {
            throw new AlreadyVotedException("cannot vote more than once");
        }

        rating = new CommentRating();

        rating.setUser(currentUser);
        rating.setValue(like ? Rating.LIKE_VALUE : Rating.DISLIKE_VALUE);
        rating.setComment(comment);

        commentRatingRepository.saveAndFlush(rating);
    }
}
