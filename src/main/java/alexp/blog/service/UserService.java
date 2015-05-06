package alexp.blog.service;

import alexp.blog.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User findByUsername(String username);

    boolean emailExists(String email);

    boolean usernameExists(String username);

    void register(User user);

    void changeEmail(String newEmail, String currentPassword) throws AuthException;

    void changePassword(String newPassword, String currentPassword) throws AuthException;

    void changeProfileInfo(User newProfileInfo);

    void authenticate(User user);

    boolean isAuthenticated();

    boolean isAdmin();

    User currentUser();

}

