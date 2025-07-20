package com.devops.springmongo.service;

import com.devops.springmongo.model.User;
import com.devops.springmongo.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        meterRegistry = new SimpleMeterRegistry();
        userService = new UserService(meterRegistry);
        userService.userRepository = userRepository;
        userService.passwordEncoder = passwordEncoder;
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(
                createTestUser("user1", "user1@test.com"),
                createTestUser("user2", "user2@test.com")
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.findAll();

        // Then
        assertEquals(expectedUsers.size(), actualUsers.size());
        verify(userRepository).findAll();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        String userId = "123";
        User expectedUser = createTestUser("testuser", "test@test.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // When
        Optional<User> actualUser = userService.findById(userId);

        // Then
        assertTrue(actualUser.isPresent());
        assertEquals(expectedUser.getUsername(), actualUser.get().getUsername());
        verify(userRepository).findById(userId);
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        String userId = "123";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> actualUser = userService.findById(userId);

        // Then
        assertFalse(actualUser.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void save_WhenValidUser_ShouldSaveAndReturnUser() {
        // Given
        User inputUser = createTestUser("newuser", "newuser@test.com");
        User savedUser = createTestUser("newuser", "newuser@test.com");
        savedUser.setId("123");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User actualUser = userService.save(inputUser);

        // Then
        assertNotNull(actualUser);
        assertEquals(savedUser.getId(), actualUser.getId());
        assertEquals(savedUser.getUsername(), actualUser.getUsername());
        verify(userRepository).existsByUsername(inputUser.getUsername());
        verify(userRepository).existsByEmail(inputUser.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void save_WhenUsernameExists_ShouldThrowException() {
        // Given
        User inputUser = createTestUser("existinguser", "test@test.com");
        when(userRepository.existsByUsername(inputUser.getUsername())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(inputUser);
        });
        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository).existsByUsername(inputUser.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void save_WhenEmailExists_ShouldThrowException() {
        // Given
        User inputUser = createTestUser("newuser", "existing@test.com");
        when(userRepository.existsByUsername(inputUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(inputUser.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(inputUser);
        });
        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository).existsByUsername(inputUser.getUsername());
        verify(userRepository).existsByEmail(inputUser.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteById_WhenUserExists_ShouldDeleteUser() {
        // Given
        String userId = "123";
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteById(userId);

        // Then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteById_WhenUserNotExists_ShouldThrowException() {
        // Given
        String userId = "123";
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteById(userId);
        });
        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        return user;
    }
}