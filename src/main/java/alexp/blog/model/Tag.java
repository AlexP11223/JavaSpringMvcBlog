package alexp.blog.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue
    private Long Id;

    @Column(length = 30, nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Collection<Post> posts = new ArrayList<>();

    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Post> getPosts() {
        return posts;
    }

    public void setPosts(Collection<Post> posts) {
        this.posts = posts;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                '}';
    }
}
