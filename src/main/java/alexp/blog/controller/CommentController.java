package alexp.blog.controller;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.service.CommentService;
import alexp.blog.service.PostService;
import alexp.blog.service.UserService;
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

        List<Comment> comments = commentService.getPostComments(post);

        model.addAttribute("comments", comments);

        return "fragments/comments :: commentList";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/posts/{postId}/comments/create", method = RequestMethod.POST)
    public @ResponseBody String addComment(@Valid @ModelAttribute(value = "comment") Comment comment, BindingResult result,
                                           @PathVariable("postId") Long postId) {
        if (result.hasErrors()) {
            return result.getAllErrors().get(0).getDefaultMessage();
        }

        Post post = postService.getPost(postId);

        if (post == null)
            return "post not found";

        if (post.isHidden() && !userService.isAdmin())
            return "post not found";

        commentService.saveNewComment(comment, post);

        return "ok";
    }

}
