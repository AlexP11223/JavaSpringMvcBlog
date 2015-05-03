package alexp.blog.repository;

import alexp.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByHiddenFalseOrHiddenIsNull(Pageable pageable);
}
