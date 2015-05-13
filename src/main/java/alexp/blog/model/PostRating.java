package alexp.blog.model;

import javax.persistence.*;

@Entity
@Table(name = "post_ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class PostRating extends Rating {

    @Id
    @GeneratedValue
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public PostRating(User user, short value, Post post) {
        super(user, value);
        this.post = post;
    }

    public PostRating() {

    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
