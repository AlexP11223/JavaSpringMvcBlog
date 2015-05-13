package alexp.blog.model;

import javax.persistence.*;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Rating {

    public static final short LIKE_VALUE = 1;
    public static final short DISLIKE_VALUE = -1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;

    protected short value;

    public Rating(User user, short value) {
        this.user = user;
        this.value = value;
    }

    public Rating() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }
}
