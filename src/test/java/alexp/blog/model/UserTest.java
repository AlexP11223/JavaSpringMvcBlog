package alexp.blog.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserTest {

    @Test
    public void testHasRole() throws Exception {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        User user = new User();

        assertThat(user.hasRole("ROLE_ADMIN"), is(equalTo(false)));

        user.getRoles().add(role);

        assertThat(user.hasRole("ROLE_ADMIN"), is(equalTo(true)));
        assertThat(user.hasRole("admin"), is(equalTo(true)));
    }
}