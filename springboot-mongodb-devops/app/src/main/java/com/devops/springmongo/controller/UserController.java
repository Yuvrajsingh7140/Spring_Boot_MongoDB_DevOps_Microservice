package com.devops.springmongo.controller;

import com.devops.springmongo.model.User;
import com.devops.springmongo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "Get all users", description = "Retrieve all users with optional pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("GET /api/users - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userService.findAll(pageable);

        logger.info("Retrieved {} users", users.getTotalElements());

        MDC.clear();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a user by their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID") @PathVariable String id) {

        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("GET /api/users/{}", id);

        return userService.findById(id)
                .map(user -> {
                    logger.info("User found: {}", user.getUsername());
                    MDC.clear();
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    logger.warn("User not found with id: {}", id);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Create new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("POST /api/users - Creating user: {}", user.getUsername());

        try {
            User savedUser = userService.save(user);
            logger.info("User created successfully: {}", savedUser.getId());

            // Remove password from response
            savedUser.setPassword(null);

            MDC.clear();
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            logger.error("Error creating user: {}", e.getMessage());
            MDC.clear();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Update user", description = "Update an existing user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "User ID") @PathVariable String id,
            @Valid @RequestBody User user) {

        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("PUT /api/users/{}", id);

        try {
            User updatedUser = userService.update(id, user);
            logger.info("User updated successfully: {}", updatedUser.getId());

            // Remove password from response
            updatedUser.setPassword(null);

            MDC.clear();
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            logger.error("Error updating user: {}", e.getMessage());
            MDC.clear();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@Parameter(description = "User ID") @PathVariable String id) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("DELETE /api/users/{}", id);

        try {
            userService.deleteById(id);
            logger.info("User deleted successfully: {}", id);

            MDC.clear();
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            MDC.clear();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Search users", description = "Search users by keyword")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("GET /api/users/search - keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.searchUsers(keyword, pageable);

        logger.info("Search returned {} users", users.getTotalElements());

        MDC.clear();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get active users", description = "Retrieve all active users")
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("GET /api/users/active");

        List<User> activeUsers = userService.findActiveUsers();
        logger.info("Retrieved {} active users", activeUsers.size());

        MDC.clear();
        return ResponseEntity.ok(activeUsers);
    }

    @Operation(summary = "Get user statistics", description = "Get user count statistics")
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        logger.info("GET /api/users/stats");

        long activeUsers = userService.countActiveUsers();

        var stats = new Object() {
            public final long activeUsers = userService.countActiveUsers();
            public final String timestamp = java.time.Instant.now().toString();
        };

        logger.info("Active users count: {}", activeUsers);

        MDC.clear();
        return ResponseEntity.ok(stats);
    }
}