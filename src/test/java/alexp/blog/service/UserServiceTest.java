package alexp.blog.service;

import alexp.blog.model.Role;
import alexp.blog.model.User;
import alexp.blog.repository.RoleRepository;
import alexp.blog.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    public static final String NAME = "name";
    public static final String EMAIL = "email";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldFindByEmail() {
        User user = new User();

        when(userRepository.findByEmail(EMAIL)).thenReturn(user);

        assertThat(userService.emailExists(EMAIL), is(equalTo(true)));

        assertThat(userService.findByEmail(EMAIL), is(equalTo(user)));

        verify(userRepository, atLeastOnce()).findByEmail(Matchers.anyString());
    }

    @Test
    public void shouldReturnNullWhenEmailNotExists() {
        assertThat(userService.emailExists(EMAIL), is(equalTo(false)));

        assertThat(userService.findByEmail(EMAIL), is(equalTo(null)));

        verify(userRepository, atLeastOnce()).findByEmail(Matchers.anyString());
    }

    @Test
    public void shouldFindByUsername() {
        User user = new User();

        when(userRepository.findByUsername(NAME)).thenReturn(user);

        assertThat(userService.usernameExists(NAME), is(equalTo(true)));

        assertThat(userService.findByUsername(NAME), is(equalTo(user)));

        verify(userRepository, atLeastOnce()).findByUsername(Matchers.anyString());
    }

    @Test
    public void shouldReturnNullWhenUsernameNotExists() {
        assertThat(userService.usernameExists(NAME), is(equalTo(false)));

        assertThat(userService.findByUsername(NAME), is(equalTo(null)));

        verify(userRepository, atLeastOnce()).findByUsername(Matchers.anyString());
    }

    @Test
    public void shouldAddNewUser() {
        Role role = new Role();

        when(roleRepository.findByName(Matchers.anyString())).thenReturn(role);

        User user = new User();

        userService.register(user);

        assertThat(user.isEnabled(), is(equalTo(true)));

        assertThat(user.getRoles().size(), is(equalTo(1)));
        assertThat(user.getRoles().get(0), is(role));

        verify(userRepository, times(1)).saveAndFlush(user);

        verify(roleRepository, times(1)).findByName(Matchers.anyString());
    }

    @Test
    public void shouldEncodePassword() {
        String password = "password";
        String encodedPassword = "encodedPassword";

        User user = new User();
        user.setPassword(password);

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        userService.register(user);

        assertThat(user.getPassword(), is(equalTo(encodedPassword)));

        verify(passwordEncoder, times(1)).encode(password);
    }

    @Test
    public void shouldLoadUserDetails() {
        User user = new User();
        user.setUsername(NAME);
        user.setPassword("123");

        List<String> role1Names = Arrays.asList("role1", "role2");
        for (String roleName : role1Names) {
            Role role = new Role();
            role.setName(roleName);
            user.getRoles().add(role);
        }

        when(userRepository.findByUsernameOrEmail(NAME, NAME)).thenReturn(user);

        UserDetails userDetails = userService.loadUserByUsername(NAME);

        List<SimpleGrantedAuthority> authorities = role1Names.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        assertThat(userDetails.getAuthorities().containsAll(authorities), is(equalTo(true)));

        verify(userRepository, times(1)).findByUsernameOrEmail(NAME, NAME);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrowUsernameNotFound() {
        userService.loadUserByUsername("notExisting");
    }

}