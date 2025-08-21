package com.openclassrooms.starterjwt.security.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserDetailsServiceImpl
 * Tests the Spring Security UserDetailsService implementation
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private String testEmail;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testEmail = "test@yoga.com";
        testDateTime = LocalDateTime.of(2025, 8, 21, 10, 0, 0);
        
        testUser = User.builder()
                .id(1L)
                .email(testEmail)
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .admin(false)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
    }

    @Test
    void testServiceCreation() {
        assertNotNull(userDetailsService);
        assertNotNull(userRepository);
    }

    // ==================== LOAD USER BY USERNAME TESTS ====================

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(testEmail);

        // Assert
        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) result;
        assertEquals(testUser.getId(), userDetails.getId());
        assertEquals(testUser.getEmail(), userDetails.getUsername());
        assertEquals(testUser.getFirstName(), userDetails.getFirstName());
        assertEquals(testUser.getLastName(), userDetails.getLastName());
        assertEquals(testUser.getPassword(), userDetails.getPassword());

        // Verify repository interaction
        verify(userRepository).findByEmail(testEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithCorrectAuthorities_WhenRegularUser() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(testEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        
        // Check authorities (should be empty for regular user)
        assertNotNull(result.getAuthorities());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithCorrectProperties_WhenAdminUser() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .email("admin@yoga.com")
                .firstName("Admin")
                .lastName("User")
                .password("adminPassword")
                .admin(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        when(userRepository.findByEmail("admin@yoga.com")).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("admin@yoga.com");

        // Assert
        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) result;
        assertEquals(adminUser.getId(), userDetails.getId());
        assertEquals(adminUser.getEmail(), userDetails.getUsername());
        assertEquals(adminUser.getFirstName(), userDetails.getFirstName());
        assertEquals(adminUser.getLastName(), userDetails.getLastName());
        assertEquals(adminUser.getPassword(), userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        String nonExistentEmail = "nonexistent@yoga.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentEmail);
        });

        assertEquals("User Not Found with email: " + nonExistentEmail, exception.getMessage());

        // Verify repository interaction
        verify(userRepository).findByEmail(nonExistentEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenEmailIsNull() {
        // Arrange
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(null);
        });

        assertEquals("User Not Found with email: null", exception.getMessage());

        // Verify repository interaction
        verify(userRepository).findByEmail(null);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenEmailIsEmpty() {
        // Arrange
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(emptyEmail);
        });

        assertEquals("User Not Found with email: ", exception.getMessage());

        // Verify repository interaction
        verify(userRepository).findByEmail(emptyEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldHandleUserWithMinimalData() {
        // Arrange
        User minimalUser = User.builder()
                .id(3L)
                .email("minimal@yoga.com")
                .firstName("Min")
                .lastName("User")
                .password("password")
                .admin(false)
                .build();

        when(userRepository.findByEmail("minimal@yoga.com")).thenReturn(Optional.of(minimalUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("minimal@yoga.com");

        // Assert
        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) result;
        assertEquals(minimalUser.getId(), userDetails.getId());
        assertEquals(minimalUser.getEmail(), userDetails.getUsername());
        assertEquals("Min", userDetails.getFirstName());
        assertEquals("User", userDetails.getLastName());
        assertEquals(minimalUser.getPassword(), userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_ShouldHandleUserWithLongEmail() {
        // Arrange
        String longEmail = "very.long.email.address.for.testing@verylongdomainname.yoga.com";
        User userWithLongEmail = User.builder()
                .id(4L)
                .email(longEmail)
                .firstName("John")
                .lastName("Doe")
                .password("password")
                .admin(false)
                .build();

        when(userRepository.findByEmail(longEmail)).thenReturn(Optional.of(userWithLongEmail));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(longEmail);

        // Assert
        assertNotNull(result);
        assertEquals(longEmail, result.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldHandleSpecialCharactersInEmail() {
        // Arrange
        String emailWithSpecialChars = "test+special@yoga-studio.com";
        User userWithSpecialEmail = User.builder()
                .id(5L)
                .email(emailWithSpecialChars)
                .firstName("Special")
                .lastName("User")
                .password("password")
                .admin(false)
                .build();

        when(userRepository.findByEmail(emailWithSpecialChars)).thenReturn(Optional.of(userWithSpecialEmail));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(emailWithSpecialChars);

        // Assert
        assertNotNull(result);
        assertEquals(emailWithSpecialChars, result.getUsername());
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void loadUserByUsername_ShouldHandleRepositoryException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userDetailsService.loadUserByUsername(testEmail);
        });

        // Verify repository was called
        verify(userRepository).findByEmail(testEmail);
        verifyNoMoreInteractions(userRepository);
    }

    // ==================== INTEGRATION SCENARIOS ====================

    @Test
    void loadUserByUsername_ShouldWorkConsistently_WhenCalledMultipleTimes() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result1 = userDetailsService.loadUserByUsername(testEmail);
        UserDetails result2 = userDetailsService.loadUserByUsername(testEmail);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getUsername(), result2.getUsername());
        assertEquals(result1.getPassword(), result2.getPassword());

        // Verify repository was called twice
        verify(userRepository, times(2)).findByEmail(testEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldHandleDifferentUsers() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .email("user2@yoga.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("password2")
                .admin(true)
                .build();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("user2@yoga.com")).thenReturn(Optional.of(user2));

        // Act
        UserDetails result1 = userDetailsService.loadUserByUsername(testEmail);
        UserDetails result2 = userDetailsService.loadUserByUsername("user2@yoga.com");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getUsername(), result2.getUsername());
        
        UserDetailsImpl userDetails1 = (UserDetailsImpl) result1;
        UserDetailsImpl userDetails2 = (UserDetailsImpl) result2;
        
        assertEquals(testUser.getId(), userDetails1.getId());
        assertEquals(user2.getId(), userDetails2.getId());
        assertEquals("John", userDetails1.getFirstName());
        assertEquals("Jane", userDetails2.getFirstName());

        // Verify interactions
        verify(userRepository).findByEmail(testEmail);
        verify(userRepository).findByEmail("user2@yoga.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldBuildUserDetailsCorrectly_WithAllFields() {
        // Arrange
        User completeUser = User.builder()
                .id(123L)
                .email("complete@yoga.com")
                .firstName("Complete")
                .lastName("User")
                .password("complexPassword123")
                .admin(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        when(userRepository.findByEmail("complete@yoga.com")).thenReturn(Optional.of(completeUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("complete@yoga.com");

        // Assert
        assertNotNull(result);
        UserDetailsImpl userDetails = (UserDetailsImpl) result;
        
        assertEquals(123L, userDetails.getId());
        assertEquals("complete@yoga.com", userDetails.getUsername());
        assertEquals("Complete", userDetails.getFirstName());
        assertEquals("User", userDetails.getLastName());
        assertEquals("complexPassword123", userDetails.getPassword());
        // Note: admin field is not mapped by UserDetailsServiceImpl
        
        // Standard UserDetails properties
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }
}