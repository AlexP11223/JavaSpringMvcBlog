package alexp.blog.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue
    private Long Id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Collection<User> users = new ArrayList<>();;

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

    public Collection<User> getUsers() {
        return users;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                '}';
    }
}
