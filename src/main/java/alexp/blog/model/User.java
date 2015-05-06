package alexp.blog.model;

import alexp.blog.service.MarkdownConverter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    public interface CreateValidationGroup {}
    public interface ChangeEmailValidationGroup {}
    public interface ChangePasswordValidationGroup {}
    public interface ProfileInfoValidationGroup {}

    @Id
    @GeneratedValue
    private Long Id;

    @Column(unique = true, nullable = false, length = 50)
    // haven't figured out how to specify messages for Size.List in the messages file
    @Size.List({
            @Size(min = 3, message = "Username too short", groups = {CreateValidationGroup.class}),
            @Size(max = 25, message = "Username too long", groups = {CreateValidationGroup.class})
    })
    @NotBlank(groups = {CreateValidationGroup.class})
    @Pattern(regexp = "^[\\p{L}0-9\\._\\- ]+$", groups = {CreateValidationGroup.class})
    private String username;

    @Column(unique = true, nullable = false, length = 50)
    @Email(groups = {CreateValidationGroup.class, ChangeEmailValidationGroup.class})
    @NotBlank(groups = {CreateValidationGroup.class, ChangeEmailValidationGroup.class})
    private String email;

    @Column(nullable = false, length = 80)
    // haven't figured out how to specify messages for Size.List in the messages file
    @Size.List({
            @Size(min = 6, message = "Password too short", groups = {CreateValidationGroup.class, ChangePasswordValidationGroup.class}),
            @Size(max = 80, message = "Password too long", groups = {CreateValidationGroup.class, ChangePasswordValidationGroup.class})
    })
    @NotBlank(groups = {CreateValidationGroup.class, ChangePasswordValidationGroup.class})
    private String password;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Date registrationDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    public List<Comment> comments = new ArrayList<>();

    @Column(nullable = true, length = 1000)
    @Size(max = 1000, groups = {ProfileInfoValidationGroup.class})
    public String aboutText;

    @Column(nullable = true, length = 80)
    @Pattern(regexp = "^\\s*(https?:\\/\\/.+)?", groups = {ProfileInfoValidationGroup.class})
    @Size(max = 80, groups = {ProfileInfoValidationGroup.class})
    public String websiteLink;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getAboutText() {
        return aboutText;
    }

    public String getAboutTextHtml() {
        return StringUtils.isEmpty(aboutText) ? "" : MarkdownConverter.toHtml(aboutText);
    }

    public void setAboutText(String aboutText) {
        this.aboutText = aboutText;
    }

    public String getWebsiteLink() {
        return websiteLink;
    }

    public String getWebsiteLinkTitle() {
        return StringUtils.isEmpty(websiteLink) ? "" :
                StringUtils.trimTrailingCharacter(websiteLink.replace("https://", "").replace("http://", "").replace("www.", ""), '/');
    }

    public void setWebsiteLink(String websiteLink) {
        this.websiteLink = websiteLink;
    }

    public boolean hasRole(String role) {
        role = role.toUpperCase();

        if (!role.startsWith("ROLE_"))
            role = "ROLE_" + role;

        final String finalRole = role;
        return getRoles().stream().anyMatch(r -> r.getName().equals(finalRole));
    }

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles +
                '}';
    }
}
