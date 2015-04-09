package alexp.blog.controller;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.service.CommentService;
import alexp.blog.service.PostService;
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
    private CommentService commentService;

    @RequestMapping(value = "/comments/show", method = RequestMethod.GET)
    public String showComments(@RequestParam(value = "postid") Long postId, ModelMap model) {
        Post post = postService.getPost(postId);

        List<Comment> comments = commentService.getPostComments(post);

        model.addAttribute("comments", comments);

        return "fragments/comments :: commentList";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/comment/add", method = RequestMethod.POST)
    public @ResponseBody String addComment(@Valid @ModelAttribute(value = "comment") Comment comment, BindingResult result,
                                           @RequestParam(value = "postid") Long postId) {
        if (result.hasErrors()) {
            return result.getAllErrors().get(0).getDefaultMessage();
        }

        Post post = postService.getPost(postId);

        commentService.saveNewComment(comment, post);

        return "ok";
    }

}
