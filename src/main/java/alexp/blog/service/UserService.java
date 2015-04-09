package alexp.blog.service;

import alexp.blog.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User findByUsername(String username);

    boolean emailExists(String email);

    boolean usernameExists(String username);

    void register(User user);

    void authenticate(User user);

    boolean isAuthenticated();

    User currentUser();

}

