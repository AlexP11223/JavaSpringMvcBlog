package alexp.blog.controller;

import alexp.blog.model.Comment;
import alexp.blog.model.Post;
import alexp.blog.model.PostEditDto;
import alexp.blog.service.PostService;
import alexp.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Controller
public class PostsController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @RequestMapping(value = {"/", "/posts"}, method = RequestMethod.GET)
    public String showPostsList(@RequestParam(value = "page", defaultValue = "0") Integer pageNumber, ModelMap model) {
        Page<Post> postsPage = postService.getPostsPage(pageNumber, 10);

        model.addAttribute("postsPage", postsPage);

        return "posts";
    }

    @RequestMapping(value = "/tag", method = RequestMethod.GET)
    public @ResponseBody String searchByTag(@RequestParam("name") String tagName, ModelMap model) {

        return "search by tag: TODO";
    }

    @RequestMapping(value = "/posts/{postId}", method = RequestMethod.GET)
    public String showPost(@PathVariable("postId") Long postId, ModelMap model) {
        Post post = postService.getPost(postId);

        if (post == null)
            throw new ResourceNotFoundException();

        if (post.isHidden() && !userService.isAdmin())
            throw new ResourceNotFoundException();

        model.addAttribute("post", post);

        if (userService.isAuthenticated()) {
            model.addAttribute("comment", new Comment());
        }

        return "post";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/posts/create", method = RequestMethod.GET)
    public String showCreatePostForm(ModelMap model) {
        model.addAttribute("post", new PostEditDto());

        model.addAttribute("edit", false);

        return "editpost";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/posts/create", method = RequestMethod.POST)
    public String createPost(ModelMap model, @Valid @ModelAttribute("post") PostEditDto post, BindingResult result) {
        if (result.hasErrors()) {
            model.addAttribute("edit", false);

            return "editpost";
        }

        postService.saveNewPost(post);

        return "redirect:/posts";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/posts/{postId}/edit", method = RequestMethod.GET)
    public String showEditPostForm(@PathVariable("postId") Long postId, ModelMap model) {
        PostEditDto post = postService.getEditablePost(postId);

        if (post == null)
            throw new ResourceNotFoundException();

        model.addAttribute("post", post);

        model.addAttribute("edit", true);

        return "editpost";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/posts/{postId}/edit", method = RequestMethod.POST)
    public String updatePost(ModelMap model, @Valid @ModelAttribute("post") PostEditDto post, BindingResult result,
                                   @PathVariable("postId") Long postId) {
        post.setId(postId);

        if (result.hasErrors()) {
            model.addAttribute("edit", true);

            return "editpost";
        }

        postService.updatePost(post);

        return "redirect:/posts/" + postId;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/posts/{postId}/hide", method = RequestMethod.POST)
    public @ResponseBody String hidePost(@PathVariable("postId") Long postId) {
        postService.setPostVisibility(postId, true);

        return "ok";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/posts/{postId}/unhide", method = RequestMethod.POST)
    public @ResponseBody String unhidePost(@PathVariable("postId") Long postId) {
        postService.setPostVisibility(postId, false);

        return "ok";
    }
}