package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Tests the business logic layer without database interaction
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testUser = User.builder()
                .id(testUserId)
                .email("test@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testServiceCreation() {
        // Verify that service is properly instantiated
        assertNotNull(userService);
        assertNotNull(userRepository);
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findById(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
        assertEquals(testUser.isAdmin(), result.isAdmin());

        // Verify repository interaction
        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findById_ShouldReturnNull_WhenUserDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        User result = userService.findById(nonExistentId);

        // Assert
        assertNull(result);

        // Verify repository interaction
        verify(userRepository).findById(nonExistentId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findById_ShouldReturnNull_WhenIdIsNull() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        User result = userService.findById(null);

        // Assert
        assertNull(result);

        // Verify repository interaction
        verify(userRepository).findById(null);
    }

    @Test
    void findById_ShouldHandleRepositoryException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.findById(testUserId);
        });

        // Verify repository was called
        verify(userRepository).findById(testUserId);
    }

    // ==================== DELETE TESTS ====================

    @Test
    void delete_ShouldCallRepositoryDeleteById_WhenIdIsValid() {
        // Arrange
        doNothing().when(userRepository).deleteById(testUserId);

        // Act
        userService.delete(testUserId);

        // Assert
        verify(userRepository).deleteById(testUserId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById_WhenIdIsNull() {
        // Arrange
        doNothing().when(userRepository).deleteById(null);

        // Act
        userService.delete(null);

        // Assert
        verify(userRepository).deleteById(null);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_ShouldPropagateRepositoryException() {
        // Arrange
        doThrow(new RuntimeException("Database constraint violation"))
                .when(userRepository).deleteById(testUserId);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.delete(testUserId);
        });

        // Verify repository was called
        verify(userRepository).deleteById(testUserId);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById_WithNonExistentId() {
        // Arrange - Repository won't throw exception for non-existent ID (JPA behavior)
        Long nonExistentId = 999L;
        doNothing().when(userRepository).deleteById(nonExistentId);

        // Act
        userService.delete(nonExistentId);

        // Assert
        verify(userRepository).deleteById(nonExistentId);
        verifyNoMoreInteractions(userRepository);
    }

    // ==================== INTEGRATION SCENARIOS TESTS ====================

    @Test
    void findById_ThenDelete_ShouldWorkInSequence() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(testUserId);

        // Act
        User foundUser = userService.findById(testUserId);
        userService.delete(foundUser.getId());

        // Assert
        assertNotNull(foundUser);
        assertEquals(testUserId, foundUser.getId());

        // Verify both operations were called
        verify(userRepository).findById(testUserId);
        verify(userRepository).deleteById(testUserId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void multipleCallsToFindById_ShouldEachCallRepository() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        User result1 = userService.findById(testUserId);
        User result2 = userService.findById(testUserId);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());

        // Verify repository was called twice
        verify(userRepository, times(2)).findById(testUserId);
        verifyNoMoreInteractions(userRepository);
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    void findById_ShouldHandleLargeId() {
        // Arrange
        Long largeId = Long.MAX_VALUE;
        when(userRepository.findById(largeId)).thenReturn(Optional.empty());

        // Act
        User result = userService.findById(largeId);

        // Assert
        assertNull(result);
        verify(userRepository).findById(largeId);
    }

    @Test
    void delete_ShouldHandleLargeId() {
        // Arrange
        Long largeId = Long.MAX_VALUE;
        doNothing().when(userRepository).deleteById(largeId);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> userService.delete(largeId));
        verify(userRepository).deleteById(largeId);
    }
}