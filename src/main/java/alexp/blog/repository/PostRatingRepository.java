package alexp.blog.repository;

import alexp.blog.model.PostRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRatingRepository extends JpaRepository<PostRating, Long> {

    @Query("SELECT r FROM PostRating r WHERE r.post.id = :postId AND r.user.id = :userId")
    PostRating findUserRating(@Param("postId") Long postId, @Param("userId") Long userId);
}
