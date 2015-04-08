package alexp.blog.service;

import alexp.blog.model.Post;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {

    Page<Post> getPostsPage(int pageNumber);

    Post getPost(Long id);

    void saveNewPost(Post post, String tagstext);
}
