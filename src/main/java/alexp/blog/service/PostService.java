package alexp.blog.service;

import alexp.blog.model.Post;
import org.springframework.data.domain.Page;

public interface PostService {

    Page<Post> getPostsPage(int pageNumber, int pageSize);

    Post getPost(Long id);

    void saveNewPost(Post post, String tagstext);
}
