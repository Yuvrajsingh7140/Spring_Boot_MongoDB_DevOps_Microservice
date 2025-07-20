package com.devops.springmongo.service;

import com.devops.springmongo.model.User;
import com.devops.springmongo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Counter userCreatedCounter;
    private final Counter userUpdatedCounter;
    private final Counter userDeletedCounter;

    public UserService(MeterRegistry meterRegistry) {
        this.userCreatedCounter = Counter.builder("users_created_total")
                .description("Total number of users created")
                .register(meterRegistry);
        this.userUpdatedCounter = Counter.builder("users_updated_total")
                .description("Total number of users updated")
                .register(meterRegistry);
        this.userDeletedCounter = Counter.builder("users_deleted_total")
                .description("Total number of users deleted")
                .register(meterRegistry);
    }

    public List<User> findAll() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    public Page<User> findAll(Pageable pageable) {
        logger.debug("Fetching users with pagination: {}", pageable);
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(String id) {
        logger.debug("Fetching user by id: {}", id);
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        logger.info("Creating new user: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        userCreatedCounter.increment();
        logger.info("User created successfully: {}", savedUser.getId());

        return savedUser;
    }

    public User update(String id, User user) {
        logger.info("Updating user: {}", id);

        return userRepository.findById(id)
            .map(existingUser -> {
                existingUser.setFirstName(user.getFirstName());
                existingUser.setLastName(user.getLastName());
                existingUser.setEmail(user.getEmail());
                existingUser.setActive(user.isActive());

                User updatedUser = userRepository.save(existingUser);
                userUpdatedCounter.increment();
                logger.info("User updated successfully: {}", updatedUser.getId());

                return updatedUser;
            })
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteById(String id) {
        logger.info("Deleting user: {}", id);

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        userDeletedCounter.increment();
        logger.info("User deleted successfully: {}", id);
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        logger.debug("Searching users with keyword: {}", keyword);
        return userRepository.searchUsers(keyword, pageable);
    }

    public long countActiveUsers() {
        return userRepository.countByActiveTrue();
    }

    public List<User> findActiveUsers() {
        return userRepository.findByActiveTrue();
    }
}