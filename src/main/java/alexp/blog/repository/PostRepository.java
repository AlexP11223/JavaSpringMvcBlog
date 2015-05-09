package alexp.blog.repository;

import alexp.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByHiddenFalse(Pageable pageable);

    // without count
    List<Post> findByHiddenIs(boolean hidden, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE :tagCount = (SELECT COUNT(DISTINCT t.id) FROM Post p2 JOIN p2.tags t WHERE LOWER(t.name) in (:tags) and p = p2)")
    Page<Post> findByTags(@Param("tags") Collection<String> tags, @Param("tagCount") Long tagCount, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE :tagCount = (SELECT COUNT(DISTINCT t.id) FROM Post p2 JOIN p2.tags t WHERE p.hidden = false and LOWER(t.name) in (:tags) and p = p2)")
    Page<Post> findByTagsAndNotHidden(@Param("tags") Collection<String> tags, @Param("tagCount") Long tagCount, Pageable pageable);
}
