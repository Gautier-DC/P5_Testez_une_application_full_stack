package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserService
 * Tests the service layer with real database interactions using H2 in-memory database
 */
@SpringBootTest
@ActiveProfiles("test")  // Use test profile for H2 database
@Transactional           // Rollback after each test to keep tests isolated
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User savedUser;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();
        
        // Create test user
        testUser = User.builder()
                .email("integration@yoga.com")
                .firstName("Integration")
                .lastName("Test")
                .password("encodedPassword123")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Save test user to database
        savedUser = userRepository.save(testUser);
    }

    @Test
    void contextLoads() {
        // Verify Spring context loads correctly
        assertNotNull(userService);
        assertNotNull(userRepository);
    }

    // ==================== FIND BY ID INTEGRATION TESTS ====================

    @Test
    void findById_ShouldReturnUser_WhenUserExistsInDatabase() {
        // Act - Call service method (which will query real database)
        User foundUser = userService.findById(savedUser.getId());

        // Assert - Verify user data is correctly retrieved
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        assertEquals(testUser.getFirstName(), foundUser.getFirstName());
        assertEquals(testUser.getLastName(), foundUser.getLastName());
        assertEquals(testUser.getPassword(), foundUser.getPassword());
        assertEquals(testUser.isAdmin(), foundUser.isAdmin());
        assertNotNull(foundUser.getCreatedAt());
        assertNotNull(foundUser.getUpdatedAt());
    }

    @Test
    void findById_ShouldReturnNull_WhenUserDoesNotExistInDatabase() {
        // Arrange - Use non-existent ID
        Long nonExistentId = 999L;

        // Act
        User foundUser = userService.findById(nonExistentId);

        // Assert
        assertNull(foundUser);
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert - Spring JPA throws exception for null ID
        assertThrows(Exception.class, () -> {
            userService.findById(null);
        });
    }

    @Test
    void findById_ShouldReturnDifferentUsers_WhenMultipleUsersExist() {
        // Arrange - Create second user
        User secondUser = User.builder()
                .email("second@yoga.com")
                .firstName("Second")
                .lastName("User")
                .password("password456")
                .admin(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        User savedSecondUser = userRepository.save(secondUser);

        // Act - Find both users
        User firstFound = userService.findById(savedUser.getId());
        User secondFound = userService.findById(savedSecondUser.getId());

        // Assert - Both users should be found and different
        assertNotNull(firstFound);
        assertNotNull(secondFound);
        assertNotEquals(firstFound.getId(), secondFound.getId());
        assertEquals("integration@yoga.com", firstFound.getEmail());
        assertEquals("second@yoga.com", secondFound.getEmail());
        assertFalse(firstFound.isAdmin());
        assertTrue(secondFound.isAdmin());
    }

    // ==================== DELETE INTEGRATION TESTS ====================

    @Test
    void delete_ShouldRemoveUserFromDatabase_WhenUserExists() {
        // Arrange - Verify user exists
        assertTrue(userRepository.findById(savedUser.getId()).isPresent());

        // Act - Delete user through service
        userService.delete(savedUser.getId());

        // Assert - User should no longer exist
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void delete_ShouldNotThrowException_WhenUserDoesNotExist() {
        // Arrange - Use non-existent ID
        Long nonExistentId = 999L;

        // Act & Assert - Should not throw exception (JPA behavior)
        assertDoesNotThrow(() -> userService.delete(nonExistentId));
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert - Spring JPA throws exception for null ID
        assertThrows(Exception.class, () -> {
            userService.delete(null);
        });
    }

    @Test
    void delete_ShouldOnlyDeleteSpecifiedUser_WhenMultipleUsersExist() {
        // Arrange - Create second user
        User secondUser = User.builder()
                .email("second@yoga.com")
                .firstName("Second")
                .lastName("User")
                .password("password456")
                .admin(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        User savedSecondUser = userRepository.save(secondUser);
        
        // Verify both users exist
        assertEquals(2, userRepository.count());

        // Act - Delete only first user
        userService.delete(savedUser.getId());

        // Assert - Only first user should be deleted
        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
        assertTrue(userRepository.findById(savedSecondUser.getId()).isPresent());
        assertEquals(1, userRepository.count());
    }

    // ==================== COMBINED OPERATION TESTS ====================

    @Test
    void findThenDelete_ShouldWorkCorrectly() {
        // Act - Find user first
        User foundUser = userService.findById(savedUser.getId());
        assertNotNull(foundUser);

        // Then delete the found user
        userService.delete(foundUser.getId());

        // Assert - User should no longer exist
        User deletedUser = userService.findById(savedUser.getId());
        assertNull(deletedUser);
    }

    @Test
    void multipleFindOperations_ShouldReturnConsistentResults() {
        // Act - Call findById multiple times
        User found1 = userService.findById(savedUser.getId());
        User found2 = userService.findById(savedUser.getId());
        User found3 = userService.findById(savedUser.getId());

        // Assert - All calls should return same data
        assertNotNull(found1);
        assertNotNull(found2);
        assertNotNull(found3);
        
        assertEquals(found1.getId(), found2.getId());
        assertEquals(found2.getId(), found3.getId());
        assertEquals(found1.getEmail(), found2.getEmail());
        assertEquals(found2.getEmail(), found3.getEmail());
    }

    // ==================== DATA PERSISTENCE TESTS ====================

    @Test
    void findById_ShouldPersistUserData_AcrossTransactions() {
        // Arrange - Get user ID
        Long userId = savedUser.getId();
        String originalEmail = savedUser.getEmail();

        // Act - Find user (this will be in a different transaction context)
        User foundUser = userService.findById(userId);

        // Assert - Data should be persisted correctly
        assertNotNull(foundUser);
        assertEquals(originalEmail, foundUser.getEmail());
        assertEquals(testUser.getFirstName(), foundUser.getFirstName());
        assertEquals(testUser.getLastName(), foundUser.getLastName());
    }

    @Test
    void repository_ShouldGenerateId_WhenUserIsSaved() {
        // Arrange - Create user without ID
        User newUser = User.builder()
                .email("newuser@yoga.com")
                .firstName("New")
                .lastName("User")
                .password("newpassword")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act - Save through repository (to test ID generation)
        User savedNewUser = userRepository.save(newUser);

        // Verify ID was generated
        assertNotNull(savedNewUser.getId());
        assertTrue(savedNewUser.getId() > 0);

        // Test service can find the newly created user
        User foundUser = userService.findById(savedNewUser.getId());
        assertNotNull(foundUser);
        assertEquals("newuser@yoga.com", foundUser.getEmail());
    }

    // ==================== EDGE CASES AND ERROR SCENARIOS ====================

    @Test
    void findById_ShouldHandleLargeIds() {
        // Arrange
        Long largeId = Long.MAX_VALUE;

        // Act
        User foundUser = userService.findById(largeId);

        // Assert
        assertNull(foundUser);
    }

    @Test
    void delete_ShouldHandleLargeIds() {
        // Arrange
        Long largeId = Long.MAX_VALUE;

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> userService.delete(largeId));
    }

    @Test
    void userService_ShouldHandleEmptyDatabase() {
        // Arrange - Clear all users
        userRepository.deleteAll();
        assertEquals(0, userRepository.count());

        // Act
        User foundUser = userService.findById(1L);

        // Assert
        assertNull(foundUser);
    }

    @Test
    void userService_ShouldWorkWithDatabaseConstraints() {
        // Note: This test would verify unique constraints, foreign key constraints, etc.
        // Implementation depends on your User entity constraints
        
        // Verify user was saved with all fields
        User foundUser = userService.findById(savedUser.getId());
        assertNotNull(foundUser);
        
        // Verify required fields are not null (based on entity constraints)
        assertNotNull(foundUser.getEmail());
        assertNotNull(foundUser.getFirstName());
        assertNotNull(foundUser.getLastName());
        assertNotNull(foundUser.getPassword());
    }
}
