package alexp.blog.controller;

import alexp.blog.model.Post;
import alexp.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class PostsController {

	@Autowired
	private PostService postService;

	@RequestMapping(value = {"/", "/posts"}, method = RequestMethod.GET)
	public String showPostsList(@RequestParam(value = "page", defaultValue = "0") Integer pageNumber, ModelMap model) {
		Page<Post> postsPage = postService.getPostsPage(pageNumber);

		model.addAttribute("postsPage", postsPage);

		return "posts";
	}

	@RequestMapping(value = "/post", method = RequestMethod.GET)
	public String showPost(@RequestParam(value = "id") Long postId, ModelMap model) {

		Post post = postService.getPost(postId);

		model.addAttribute("post", post);

		return "post";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/post/create", method = RequestMethod.GET)
	public String showPostForm(ModelMap model) {
		model.addAttribute("post", new Post());

		return "createpost";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/post/create", method = RequestMethod.POST)
	public String createPost(@Valid @ModelAttribute(value = "post") Post post, BindingResult result,
							 @RequestParam(value = "tagstext", defaultValue = "") String tagstext) {
		if (result.hasErrors()) {
			return "createpost";
		}

		postService.saveNewPost(post, tagstext);


		return "redirect:/posts";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/post/edit")
	public @ResponseBody String editPost() {
		return "TODO";
	}
}