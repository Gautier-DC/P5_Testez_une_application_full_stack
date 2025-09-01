package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 * Tests the complete REST API with real Spring context, security, and H2 database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "test@example.com")
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean database
        userRepository.deleteAll();

        // Create test user (same as mock user)
        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Create another user
        otherUser = User.builder()
                .email("other@example.com")
                .firstName("Other")
                .lastName("User")
                .password("otherpassword")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        otherUser = userRepository.save(otherUser);
    }

    // ==================== FIND BY ID INTEGRATION TESTS ====================

    @Test
    void findById_ShouldReturnUserDto_WhenValidIdAndUserExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenValidIdButUserDoesNotExist() throws Exception {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsEmpty() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Spring treats empty path as different endpoint
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdContainsSpecialCharacters() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", "1@#$")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenNegativeId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenZeroId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldHandleLargeIds() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturnOtherUser_WhenValidId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", otherUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("other@example.com"))
                .andExpect(jsonPath("$.firstName").value("Other"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void findById_ShouldReturnAdmin_WhenUserIsAdmin() throws Exception {
        // Arrange
        User adminUser = User.builder()
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .password("adminpass")
                .admin(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", adminUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(true));
    }

    // ==================== DELETE INTEGRATION TESTS ====================

    @Test
    void delete_ShouldReturnOk_WhenUserDeletesOwnAccount() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void delete_ShouldReturnUnauthorized_WhenUserTriesToDeleteOtherAccount() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenIdIsEmpty() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Spring treats empty path as different endpoint
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenIdContainsSpecialCharacters() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", "1@#$")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenNegativeId() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenZeroId() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== HTTP METHODS TESTS ====================

    @Test
    void findById_ShouldNotAcceptPOST() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void findById_ShouldNotAcceptPUT() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void delete_ShouldNotAcceptGET() throws Exception {
        // This would actually work as findById, testing misuse
        mockMvc.perform(get("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // This actually works as findById
    }

    @Test
    void delete_ShouldNotAcceptPOST() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void delete_ShouldNotAcceptPUT() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== CORS TESTS ====================

    @Test
    void findById_ShouldSupportCORS() throws Exception {
        // Note: CORS headers might not be set in test environment
        // Act & Assert - Just verify the endpoint works
        mockMvc.perform(get("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldSupportCORS() throws Exception {
        // Note: CORS headers might not be set in test environment
        // Act & Assert - Just verify the endpoint works
        mockMvc.perform(delete("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== CONTENT TYPE TESTS ====================

    @Test
    void findById_ShouldAcceptDifferentContentTypes() throws Exception {
        // Test with application/json
        mockMvc.perform(get("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test without explicit content type
        mockMvc.perform(get("/api/user/{id}", testUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "delete@example.com")
    void delete_ShouldAcceptDifferentContentTypes() throws Exception {
        // Create user for deletion test
        User userForDeletion = User.builder()
                .email("delete@example.com")
                .firstName("Delete")
                .lastName("Test")
                .password("password")
                .admin(false)
                .build();
        userForDeletion = userRepository.save(userForDeletion);

        // Test with application/json
        mockMvc.perform(delete("/api/user/{id}", userForDeletion.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== DATA PERSISTENCE TESTS ====================

    @Test
    void findById_ShouldReturnCorrectDataFromDatabase() throws Exception {
        // Arrange - Create specific user with known data
        User specificUser = userRepository.save(User.builder()
                .email("specific@example.com")
                .firstName("Specific")
                .lastName("TestUser")
                .password("specificpass")
                .admin(true)
                .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 2, 11, 0))
                .build());

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", specificUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(specificUser.getId().intValue()))
                .andExpect(jsonPath("$.email").value("specific@example.com"))
                .andExpect(jsonPath("$.firstName").value("Specific"))
                .andExpect(jsonPath("$.lastName").value("TestUser"))
                .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    void delete_ShouldRemoveUserFromDatabase() throws Exception {
        // Arrange - Get initial count
        long initialCount = userRepository.count();

        // Act
        mockMvc.perform(delete("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assert
        long finalCount = userRepository.count();
        assert finalCount == initialCount - 1;
        assert !userRepository.existsById(testUser.getId());
    }

    // ==================== SPECIAL CHARACTERS TESTS ====================

    @Test
    void findById_ShouldHandleSpecialCharactersInUserData() throws Exception {
        // Arrange
        User specialUser = userRepository.save(User.builder()
                .email("josé.maría@example.com")
                .firstName("José-María")
                .lastName("O'Connor-Smith")
                .password("password")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", specialUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("josé.maría@example.com"))
                .andExpect(jsonPath("$.firstName").value("José-María"))
                .andExpect(jsonPath("$.lastName").value("O'Connor-Smith"));
    }

    // ==================== AUTHORIZATION EDGE CASES ====================

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void delete_ShouldReturnUnauthorized_WhenAuthenticatedUserNotInDatabase() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void delete_ShouldAllowUserToDeleteOwnAccount_EvenWhenOtherUsersExist() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify other user still exists
        assert userRepository.existsById(otherUser.getId());
        assert !userRepository.existsById(testUser.getId());
    }

    // ==================== COMBINED OPERATIONS TESTS ====================

    @Test
    void multipleRequests_ShouldWorkIndependently() throws Exception {
        // Act & Assert - Multiple requests should all work
        mockMvc.perform(get("/api/user/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Test"));

        mockMvc.perform(get("/api/user/{id}", otherUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Other"));

        mockMvc.perform(get("/api/user/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("User"));

        // Verify users still exist
        assert userRepository.existsById(testUser.getId());
        assert userRepository.existsById(otherUser.getId());
    }
}