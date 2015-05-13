package alexp.blog.model;

import alexp.blog.service.MarkdownConverter;
import alexp.blog.utils.LocalDateTimePersistenceConverter;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Column(nullable = true)
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime modifiedDateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private boolean deleted = false;

    // simple (adjacency list) comments hierarchy implementation
    // probably not the most performance efficient choice since it will have additional DB queries for each level in each subtree

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @OneToMany(mappedBy="parentComment", cascade = CascadeType.ALL)
    @OrderBy("dateTime ASC")
    private List<Comment> childrenComments = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "comment")

    public List<CommentRating> commentRatings = new ArrayList<>();

    public int commentLevel() {
        Comment comment = this;
        int level = 0;
        while ((comment = comment.getParentComment()) != null)
            level++;
        return level;
    }

    public boolean userCanDelete() {
        return LocalDateTime.now().isBefore(maxDeleteTime());
    }

    public LocalDateTime maxDeleteTime() {
        return dateTime.plusMinutes(10);
    }

    // should refactor to store dates in UTC in database

    public long maxDeleteTimeUnixTimestamp() {
        return maxDeleteTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public boolean userCanEdit() {
        return LocalDateTime.now().isBefore(maxEditTime());
    }

    public LocalDateTime maxEditTime() {
        return dateTime.plusMinutes(180);
    }

    public long maxEditTimeUnixTimestamp() {
        return maxEditTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public int getRatingSum() {
        return commentRatings.stream().mapToInt(Rating::getValue).sum();
    }

    public short getUserVoteValue(Long userId) {
        if (userId == null)
            return 0;

        Optional<CommentRating> rating = commentRatings.stream().filter(r -> r.getUser().getId().equals(userId)).findFirst();
        return rating.isPresent() ? rating.get().getValue() : 0;
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

    public LocalDateTime getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setModifiedDateTime(LocalDateTime modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
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

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public List<Comment> getChildrenComments() {
        return childrenComments;
    }

    public void setChildrenComments(List<Comment> childrenComments) {
        this.childrenComments = childrenComments;
    }

    public List<CommentRating> getCommentRatings() {
        return commentRatings;
    }

    public void setCommentRatings(List<CommentRating> commentRatings) {
        this.commentRatings = commentRatings;
    }
}
