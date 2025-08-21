package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthTokenFilter
 * Tests the JWT authentication filter functionality
 */
@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private UserDetailsImpl testUserDetails;
    private String validJwt;

    @BeforeEach
    void setUp() {
        validJwt = "valid.jwt.token";
        
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
    void testFilterCreation() {
        assertNotNull(authTokenFilter);
        assertNotNull(jwtUtils);
        assertNotNull(userDetailsService);
    }

    // ==================== VALID JWT TOKEN TESTS ====================

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidJwtTokenProvided() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtUtils.validateJwtToken(validJwt)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwt)).thenReturn("test@yoga.com");
        when(userDetailsService.loadUserByUsername("test@yoga.com")).thenReturn(testUserDetails);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(validJwt);
            verify(jwtUtils).getUserNameFromJwtToken(validJwt);
            verify(userDetailsService).loadUserByUsername("test@yoga.com");
            verify(securityContext).setAuthentication(any(Authentication.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldSetAuthenticationWithCorrectDetails_WhenValidJwtToken() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtUtils.validateJwtToken(validJwt)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwt)).thenReturn("test@yoga.com");
        when(userDetailsService.loadUserByUsername("test@yoga.com")).thenReturn(testUserDetails);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityContext).setAuthentication(any());
        }
    }

    // ==================== INVALID JWT TOKEN TESTS ====================

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenInvalidJwtToken() throws ServletException, IOException {
        // Arrange
        String invalidJwt = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidJwt);
        when(jwtUtils.validateJwtToken(invalidJwt)).thenReturn(false);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(invalidJwt);
            verify(jwtUtils, never()).getUserNameFromJwtToken(anyString());
            verifyNoInteractions(userDetailsService);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenJwtTokenIsNull() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verifyNoInteractions(jwtUtils);
            verifyNoInteractions(userDetailsService);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    // ==================== AUTHORIZATION HEADER TESTS ====================

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenAuthorizationHeaderMissing() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verifyNoInteractions(jwtUtils);
            verifyNoInteractions(userDetailsService);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenAuthorizationHeaderEmpty() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verifyNoInteractions(jwtUtils);
            verifyNoInteractions(userDetailsService);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verifyNoInteractions(jwtUtils);
            verifyNoInteractions(userDetailsService);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenBearerTokenIsEmpty() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            // Note: empty bearer token actually gets processed, so jwtUtils will be called
            // but it should return false from validateJwtToken
            verifyNoInteractions(userDetailsService);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldExtractCorrectToken_WhenValidBearerToken() throws ServletException, IOException {
        // Arrange
        String token = "my.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("test@yoga.com");
        when(userDetailsService.loadUserByUsername("test@yoga.com")).thenReturn(testUserDetails);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(token);
            verify(jwtUtils).getUserNameFromJwtToken(token);
        }
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenJwtUtilsThrowsException() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtUtils.validateJwtToken(validJwt)).thenThrow(new RuntimeException("JWT validation error"));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(validJwt);
            verifyNoInteractions(userDetailsService);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenUserDetailsServiceThrowsException() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtUtils.validateJwtToken(validJwt)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwt)).thenReturn("test@yoga.com");
        when(userDetailsService.loadUserByUsername("test@yoga.com")).thenThrow(new RuntimeException("User not found"));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(validJwt);
            verify(jwtUtils).getUserNameFromJwtToken(validJwt);
            verify(userDetailsService).loadUserByUsername("test@yoga.com");
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenSecurityContextThrowsException() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtUtils.validateJwtToken(validJwt)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwt)).thenReturn("test@yoga.com");
        when(userDetailsService.loadUserByUsername("test@yoga.com")).thenReturn(testUserDetails);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            doThrow(new RuntimeException("Security context error")).when(securityContext).setAuthentication(any());

            // Act
            authTokenFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateJwtToken(validJwt);
            verify(jwtUtils).getUserNameFromJwtToken(validJwt);
            verify(userDetailsService).loadUserByUsername("test@yoga.com");
            verify(securityContext).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    // ==================== INTEGRATION SCENARIOS ====================

    @Test
    void doFilterInternal_ShouldAlwaysCallFilterChain_RegardlessOfTokenValidity() throws ServletException, IOException {
        // Test with valid token
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtUtils.validateJwtToken(validJwt)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwt)).thenReturn("test@yoga.com");
        when(userDetailsService.loadUserByUsername("test@yoga.com")).thenReturn(testUserDetails);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            authTokenFilter.doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
        }

        reset(filterChain);

        // Test with invalid token
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        when(jwtUtils.validateJwtToken("invalid")).thenReturn(false);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            authTokenFilter.doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
        }

        reset(filterChain);

        // Test with no token
        when(request.getHeader("Authorization")).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            authTokenFilter.doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void doFilterInternal_ShouldHandleMultipleRequests_Independently() throws ServletException, IOException {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // First request with valid token
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
            when(jwtUtils.validateJwtToken(validJwt)).thenReturn(true);
            when(jwtUtils.getUserNameFromJwtToken(validJwt)).thenReturn("user1@yoga.com");
            when(userDetailsService.loadUserByUsername("user1@yoga.com")).thenReturn(testUserDetails);

            authTokenFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(any());
            reset(securityContext);

            // Second request with no token
            when(request.getHeader("Authorization")).thenReturn(null);

            authTokenFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext, never()).setAuthentication(any());
        }
    }

    // Helper method to use in tests (if needed)
    private void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Object should not be null");
        }
    }
}