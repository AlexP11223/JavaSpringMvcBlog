package alexp.blog.service;

import alexp.blog.model.Role;
import alexp.blog.model.User;
import alexp.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User findByUsername(String username);

    boolean emailExists(String email);

    boolean usernameExists(String username);

    void register(User user);

    void authenticate(User user);

    boolean isAuthenticated();

}

