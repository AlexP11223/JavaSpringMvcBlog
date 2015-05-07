package alexp.blog.model;

import alexp.blog.service.MarkdownConverter;
import alexp.blog.utils.LocalDateTimePersistenceConverter;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.time.*;
import java.util.Date;

@Entity
@Table(name = "comments")
public class Comment {

    @javax.persistence.Id
    @GeneratedValue
    private Long Id;

    @Lob
    @Column(nullable = false)
    @NotBlank
    private String commentText;

    @Column(nullable = false)
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private boolean deleted = false;

    public boolean userCanDelete() {
        return LocalDateTime.now().isBefore(maxDeleteTime());
    }

    public LocalDateTime maxDeleteTime() {
        return dateTime.plusMinutes(2);
    }

    // should refactor to store dates in UTC in database

    public long maxDeleteTimeUnixTimestamp() {
        return maxDeleteTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getCommentTextHtml() {
        return MarkdownConverter.toHtml(getCommentText());
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
