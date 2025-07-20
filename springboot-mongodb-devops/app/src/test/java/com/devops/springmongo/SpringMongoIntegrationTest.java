package com.devops.springmongo;

import com.devops.springmongo.model.User;
import com.devops.springmongo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureWebMvc
class SpringMongoIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5")
            .withExposedPorts(27017);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpected(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() throws Exception {
        // Given
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        userRepository.save(user1);
        userRepository.save(user2);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.content").isArray())
                .andExpected(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        // Given
        User savedUser = userRepository.save(createTestUser("testuser", "test@example.com"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.username").value("testuser"))
                .andExpected(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{id}", "nonexistent"))
                .andExpected(status().isNotFound());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldReturnOk() throws Exception {
        // Given
        User savedUser = userRepository.save(createTestUser("testuser", "test@example.com"));

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andExpected(status().isOk());
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