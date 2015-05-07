package alexp.blog.utils;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public class SecurityUtils {

    public static RequestPostProcessor userAlice() {
        return user("Alice").roles("USER");
    }

    public static RequestPostProcessor userBob() {
        return user("Bob").roles("USER");
    }

    public static RequestPostProcessor userAdmin() {
        return user("admin").roles("USER", "ADMIN");
    }
}
