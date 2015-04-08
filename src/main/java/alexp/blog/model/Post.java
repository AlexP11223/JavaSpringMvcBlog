package alexp.blog.model;

import com.github.rjeschke.txtmark.Processor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue
    private Long Id;

    @Column(length = 250, nullable = false)
    // haven't figured out how to specify messages for Size.List in the messages file
    @Size.List({
            @Size(min = 3, message = "Title too short"),
            @Size(max = 250, message = "Title too long")
    })
    @NotBlank
    private String title;

    @Lob
    private String shortTextPart;

    @Lob
    @Column(nullable = false)
    @NotBlank
    @Size(min = 50)
    private String fullPostText;

    @Column(columnDefinition="DATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "posts_tags",
            joinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
    private Collection<Tag> tags = new ArrayList<>();

    public static String shortPartSeparator() {
        return "===cut===";
    }

    public boolean hasShortTextPart() {
        return !StringUtils.isEmpty(shortTextPart);
    }

    public String shortTextPartHtml() {
        return Processor.process(getShortTextPart(), true);
    }

    public String fullPostTextHtml() {
        return Processor.process(getFullPostText().replace(shortPartSeparator(), ""), true);
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
}
