package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController
 * Tests the authentication REST API layer with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest testLoginRequest;
    private SignupRequest testSignupRequest;
    private User testUser;
    private UserDetailsImpl testUserDetails;

    @BeforeEach
    void setUp() {
        testLoginRequest = new LoginRequest();
        testLoginRequest.setEmail("test@yoga.com");
        testLoginRequest.setPassword("password123");

        testSignupRequest = new SignupRequest();
        testSignupRequest.setEmail("newuser@yoga.com");
        testSignupRequest.setFirstName("John");
        testSignupRequest.setLastName("Doe");
        testSignupRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .email("test@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .admin(false)
                .build();
    }

    @Test
    void testControllerCreation() {
        assertNotNull(authController);
        assertNotNull(authenticationManager);
        assertNotNull(jwtUtils);
        assertNotNull(passwordEncoder);
        assertNotNull(userRepository);
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void authenticateUser_ShouldReturnJwtResponse_WhenValidCredentials() {
        // Arrange
        String testJwt = "test.jwt.token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(testJwt);
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        when(userRepository.findByEmail("test@yoga.com")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authController.authenticateUser(testLoginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(JwtResponse.class, response.getBody());
        
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals(testJwt, jwtResponse.getToken());
        assertEquals("Bearer", jwtResponse.getType());
        assertEquals(1L, jwtResponse.getId());
        assertEquals("test@yoga.com", jwtResponse.getUsername());
        assertEquals("John", jwtResponse.getFirstName());
        assertEquals("Doe", jwtResponse.getLastName());
        assertEquals(false, jwtResponse.getAdmin());

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
        verify(userRepository).findByEmail("test@yoga.com");
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponseWithAdminTrue_WhenUserIsAdmin() {
        // Arrange
        String testJwt = "test.jwt.token";
        User adminUser = User.builder()
                .id(1L)
                .email("admin@yoga.com")
                .firstName("Admin")
                .lastName("User")
                .password("encodedPassword")
                .admin(true)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(testJwt);
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        when(userRepository.findByEmail("test@yoga.com")).thenReturn(Optional.of(adminUser));

        // Act
        ResponseEntity<?> response = authController.authenticateUser(testLoginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals(true, jwtResponse.getAdmin());
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponseWithAdminFalse_WhenUserNotFoundInRepository() {
        // Arrange
        String testJwt = "test.jwt.token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(testJwt);
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        when(userRepository.findByEmail("test@yoga.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.authenticateUser(testLoginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals(false, jwtResponse.getAdmin());
    }

    @Test
    void authenticateUser_ShouldThrowException_WhenInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authController.authenticateUser(testLoginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(userRepository);
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void registerUser_ShouldReturnSuccessMessage_WhenValidSignupRequest() {
        // Arrange
        when(userRepository.existsByEmail("newuser@yoga.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        ResponseEntity<?> response = authController.registerUser(testSignupRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(MessageResponse.class, response.getBody());
        
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("User registered successfully!", messageResponse.getMessage());

        // Verify interactions
        verify(userRepository).existsByEmail("newuser@yoga.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnErrorMessage_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("newuser@yoga.com")).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(testSignupRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(MessageResponse.class, response.getBody());
        
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error: Email is already taken!", messageResponse.getMessage());

        // Verify interactions
        verify(userRepository).existsByEmail("newuser@yoga.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldCreateUserWithCorrectProperties() {
        // Arrange
        when(userRepository.existsByEmail("newuser@yoga.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");

        // Act
        authController.registerUser(testSignupRequest);

        // Assert - Verify the user object passed to save has correct properties
        verify(userRepository).save(argThat(user -> 
            user.getEmail().equals("newuser@yoga.com") &&
            user.getFirstName().equals("John") &&
            user.getLastName().equals("Doe") &&
            user.getPassword().equals("encodedPassword123") &&
            !user.isAdmin()
        ));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void authenticateUser_ShouldHandleJwtGenerationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenThrow(new RuntimeException("JWT generation failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authController.authenticateUser(testLoginRequest);
        });
    }

    @Test
    void registerUser_ShouldHandleRepositoryException() {
        // Arrange
        when(userRepository.existsByEmail("newuser@yoga.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authController.registerUser(testSignupRequest);
        });
    }

    @Test
    void registerUser_ShouldHandlePasswordEncodingException() {
        // Arrange
        when(userRepository.existsByEmail("newuser@yoga.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenThrow(new RuntimeException("Encoding failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authController.registerUser(testSignupRequest);
        });
    }

    // ==================== INTEGRATION SCENARIOS ====================

    @Test
    void registerThenLogin_ShouldWorkInSequence() {
        // Arrange - Register
        when(userRepository.existsByEmail("newuser@yoga.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Arrange - Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("newuser@yoga.com");
        loginRequest.setPassword("password123");
        
        UserDetailsImpl newUserDetails = UserDetailsImpl.builder()
                .id(2L)
                .username("newuser@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        User savedUser = User.builder()
                .id(2L)
                .email("newuser@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .admin(false)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("new.jwt.token");
        when(authentication.getPrincipal()).thenReturn(newUserDetails);
        when(userRepository.findByEmail("newuser@yoga.com")).thenReturn(Optional.of(savedUser));

        // Act
        ResponseEntity<?> registerResponse = authController.registerUser(testSignupRequest);
        ResponseEntity<?> loginResponse = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        
        assertInstanceOf(MessageResponse.class, registerResponse.getBody());
        assertInstanceOf(JwtResponse.class, loginResponse.getBody());
    }
}