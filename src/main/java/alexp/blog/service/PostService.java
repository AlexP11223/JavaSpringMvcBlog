package alexp.blog.service;

import alexp.blog.model.Post;
import alexp.blog.model.PostEditDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {

    Page<Post> getPostsPage(int pageNumber, int pageSize);

    Post getPost(Long id);

    PostEditDto getEditablePost(Long id);

    Page<Post> findPostsByTag(List<String> tags, int pageNumber, int pageSize);

    Post saveNewPost(PostEditDto postEditDto);

    Post updatePost(PostEditDto postEditDto);

    void setPostVisibility(Long postId, boolean hide);

    void deletePost(Long postId);
}
