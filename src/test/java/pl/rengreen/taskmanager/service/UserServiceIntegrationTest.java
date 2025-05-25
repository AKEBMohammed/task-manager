package pl.rengreen.taskmanager.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.rengreen.taskmanager.config.TestContainersConfig;
import pl.rengreen.taskmanager.model.Role;
import pl.rengreen.taskmanager.model.User;
import pl.rengreen.taskmanager.repository.RoleRepository;
import pl.rengreen.taskmanager.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = {TestContainersConfig.Initializer.class})
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void shouldCreateUser() {
        // Given
        User newUser = new User();
        newUser.setName("testuser");
        newUser.setPassword("password");
        newUser.setEmail("testuser@test.com");

        // Create USER role if it doesn't exist
        Role userRole = roleRepository.findByRole("USER");
        if (userRole == null) {
            Role role = new Role();
            role.setRole("USER");
            roleRepository.save(role);
        }

        // When
        userService.createUser(newUser);

        // Then
        User savedUser = userRepository.findByEmail("testuser@test.com");
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser@test.com");
        assertThat(savedUser.getRoles()).extracting(Role::getRole).contains("USER");
    }

    @Test
    public void shouldFindAllUsers() {
        // Given
        createTestUser("user1");
        createTestUser("user2");
        createTestUser("user3");

        // When
        List<User> users = userService.findAll();

        // Then
        assertThat(users).isNotNull();
        assertThat(users.size()).isGreaterThanOrEqualTo(3);
        assertThat(users).extracting(User::getName).contains("user1", "user2", "user3");
    }

    @Test
    public void shouldFindUserById() {
        // Given
        User user = createTestUser("userToFind");
        Long userId = user.getId();

        // When
        User foundUser = userService.getUserById(userId);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(userId);
        assertThat(foundUser.getName()).isEqualTo("userToFind");
        assertThat(foundUser.getEmail()).isEqualTo("userToFind@test.com");
    }

    @Test
    public void shouldFindUserByEmail() {
        // Given
        createTestUser("emailUser");
        String email = "emailUser@test.com";

        // When
        User foundUser = userService.getUserByEmail(email);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getName()).isEqualTo("emailUser");
    }

    @Test
    public void shouldDeleteUser() {
        // Given
        User user = createTestUser("userToDelete");
        Long userId = user.getId();

        // When
        userService.deleteUser(userId);

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser.isPresent()).isFalse();
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setName(username);
        user.setPassword("password");
        user.setEmail(username + "@test.com");

        // Create USER role if it doesn't exist
        Role userRole = roleRepository.findByRole("USER");
        if (userRole == null) {
            Role role = new Role();
            role.setRole("USER");
            roleRepository.save(role);
        }

        return userRepository.save(user);
    }
}