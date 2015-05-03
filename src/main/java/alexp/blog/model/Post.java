package alexp.blog.model;

import alexp.blog.service.MarkdownConverter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue
    private Long Id;

    @Column(length = 250, nullable = false)
    private String title;

    @Lob
    private String shortTextPart;

    @Lob
    @Column(nullable = false)
    private String fullPostText;

    @Column(columnDefinition="DATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;

    @Column(nullable = false)
    private boolean hidden = false;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "posts_tags",
            joinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
    @OrderBy("name ASC")
    private Collection<Tag> tags = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "post")
    @OrderBy("dateTime ASC")
    public List<Comment> comments = new ArrayList<>();

    public static String shortPartSeparator() {
        return "===cut===";
    }

    public boolean hasShortTextPart() {
        return !StringUtils.isEmpty(shortTextPart);
    }

    public String shortTextPartHtml() {
        return MarkdownConverter.toHtml(getShortTextPart());
    }

    public String fullPostTextHtml() {
        return MarkdownConverter.toHtml(getFullPostText().replace(shortPartSeparator(), ""));
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortTextPart() {
        return shortTextPart;
    }

    public void setShortTextPart(String shortTextPart) {
        this.shortTextPart = shortTextPart;
    }

    public String getFullPostText() {
        return fullPostText;
    }

    public void setFullPostText(String fullPostText) {
        this.fullPostText = fullPostText;
    }

    public Collection<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
