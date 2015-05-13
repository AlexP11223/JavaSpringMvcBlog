package alexp.blog.controller;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.service.*;
import alexp.blog.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(value = "/posts/{postId}/comments", method = RequestMethod.GET)
    public String showComments(@PathVariable("postId") Long postId, ModelMap model) {
        Post post = postService.getPost(postId);

        if (post == null)
            throw new ResourceNotFoundException();

        if (post.isHidden() && !userService.isAdmin())
            throw new ResourceNotFoundException();

        List<Comment> comments = post.topLevelComments();

        model.addAttribute("comments", comments);
        model.addAttribute("post", post);

        return "fragments/comments :: commentList";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/posts/{postId}/comments/create", method = RequestMethod.POST)
    public @ResponseBody String addComment(@Valid @ModelAttribute(value = "comment") Comment comment, BindingResult result,
                                           @PathVariable("postId") Long postId,
                                           @RequestParam(value = "parentId", defaultValue = "") Long parentId) {
        if (result.hasErrors()) {
            return makeCommentAddResponse("error", result.getAllErrors().get(0).getDefaultMessage());
        }

        Post post = postService.getPost(postId);

        if (post == null)
            return makeCommentAddResponse("error", "post not found");

        if (post.isHidden() && !userService.isAdmin())
            return makeCommentAddResponse("error", "post not found");

        Long addedId = commentService.saveNewComment(comment, post, parentId);

        return makeCommentAddResponse("ok", addedId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/posts/{postId}/comments/{commentId}/delete", method = RequestMethod.POST)
    public @ResponseBody String deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        try {
            commentService.deleteComment(commentId);
        } catch (ActionExpiredException e) {
            return "expired";
        }

        return "ok";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/posts/{postId}/comments/{commentId}/edit", method = RequestMethod.POST)
    public @ResponseBody String editComment(@Valid @ModelAttribute(value = "comment") Comment comment, BindingResult result,
                                            @PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        if (result.hasErrors()) {
            return result.getAllErrors().get(0).getDefaultMessage();
        }

        try {
            commentService.updateComment(comment, commentId);
        } catch (ActionExpiredException e) {
            return "expired";
        }

        return "ok";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/posts/{postId}/comments/{commentId}/source", method = RequestMethod.GET,
            produces = "text/plain; charset=utf-8")
    public @ResponseBody String getCommentSource(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        Comment comment = commentService.getComment(commentId);

        if (comment == null)
            throw new ResourceNotFoundException();

        return comment.getCommentText();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/posts/{postId}/comments/{commentId}/like", method = RequestMethod.POST)
    public @ResponseBody String like(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        try {
            commentService.vote(commentId, true);
        } catch (AlreadyVotedException e) {
            return "already_voted";
        }
        catch (ForbiddenException e) {
            return "own_comment";
        }

        return "ok";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/posts/{postId}/comments/{commentId}/dislike", method = RequestMethod.POST)
    public @ResponseBody String dislike(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        try {
            commentService.vote(commentId, false);
        } catch (AlreadyVotedException e) {
            return "already_voted";
        }
        catch (ForbiddenException e) {
            return "own_comment";
        }

        return "ok";
    }

    private String makeCommentAddResponse(String status, String msg, Long id) {
        return "{" + JsonUtils.toJsonField("status", status) +
                (id == null ? "" : (", " + JsonUtils.toJsonField("id", id.toString()))) +
                (msg == null ? "" : (", " + JsonUtils.toJsonField("message", msg))) +
                "}";
    }

    private String makeCommentAddResponse(String status, Long id) {
        return makeCommentAddResponse(status, null, id);
    }

    private String makeCommentAddResponse(String status, String msg) {
        return makeCommentAddResponse(status, msg, null);
    }

    private String makeCommentAddResponse(String status) {
        return makeCommentAddResponse(status, null, null);
    }

}
