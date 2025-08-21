package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtUtils
 * Tests the JWT token generation, validation, and parsing functionality
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtUtils jwtUtils;

    private UserDetailsImpl testUserDetails;
    private String testJwtSecret;
    private int testJwtExpirationMs;

    @BeforeEach
    void setUp() {
        testJwtSecret = "testSecretKeyThatIsLongEnoughForHS512AlgorithmAndMeetsMinimumSecurityRequirementsAndHasAtLeast512Bits";
        testJwtExpirationMs = 86400000; // 24 hours in milliseconds

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testJwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", testJwtExpirationMs);

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
    void testUtilsCreation() {
        assertNotNull(jwtUtils);
        assertNotNull(authentication);
    }

    // ==================== GENERATE JWT TOKEN TESTS ====================

    @Test
    void generateJwtToken_ShouldReturnValidToken_WhenValidAuthentication() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(testUserDetails);

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));

        // Verify token structure (should have 3 parts separated by dots)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);

        // Verify we can extract the username from the generated token
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals("test@yoga.com", extractedUsername);
    }

    @Test
    void generateJwtToken_ShouldCreateTokenWithCorrectExpiration() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        Date beforeGeneration = new Date();

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);

        // Parse token to check expiration
        Date expiration = Jwts.parser()
                .setSigningKey(testJwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        Date expectedExpiration = new Date(beforeGeneration.getTime() + testJwtExpirationMs);
        
        // Allow for small time differences (within 1 second)
        long timeDifference = Math.abs(expiration.getTime() - expectedExpiration.getTime());
        assertTrue(timeDifference < 1000, "Expiration time should be within 1 second of expected");
    }

    @Test
    void generateJwtToken_ShouldCreateTokenWithCorrectSubject() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(testUserDetails);

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        String subject = Jwts.parser()
                .setSigningKey(testJwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        assertEquals("test@yoga.com", subject);
    }

    @Test
    void generateJwtToken_ShouldThrowException_WhenAuthenticationIsNull() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtUtils.generateJwtToken(authentication);
        });
    }

    // ==================== GET USERNAME FROM JWT TOKEN TESTS ====================

    @Test
    void getUserNameFromJwtToken_ShouldReturnUsername_WhenValidToken() {
        // Arrange
        String validToken = createValidToken("test@yoga.com");

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(validToken);

        // Assert
        assertEquals("test@yoga.com", username);
    }

    @Test
    void getUserNameFromJwtToken_ShouldReturnDifferentUsername_WhenDifferentTokenSubject() {
        // Arrange
        String validToken = createValidToken("admin@yoga.com");

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(validToken);

        // Assert
        assertEquals("admin@yoga.com", username);
    }

    @Test
    void getUserNameFromJwtToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtils.getUserNameFromJwtToken(invalidToken);
        });
    }

    @Test
    void getUserNameFromJwtToken_ShouldThrowException_WhenTokenIsNull() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtils.getUserNameFromJwtToken(null);
        });
    }

    @Test
    void getUserNameFromJwtToken_ShouldThrowException_WhenTokenIsEmpty() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtils.getUserNameFromJwtToken("");
        });
    }

    // ==================== VALIDATE JWT TOKEN TESTS ====================

    @Test
    void validateJwtToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Arrange
        String validToken = createValidToken("test@yoga.com");

        // Act
        boolean isValid = jwtUtils.validateJwtToken(validToken);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_WhenTokenIsNull() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_WhenTokenIsEmpty() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_WhenTokenHasInvalidSignature() {
        // Arrange - Create token with different secret
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("test@yoga.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + testJwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, "wrongSecretKeyThatIsLongEnoughForHS512AlgorithmAndMeetsMinimumSecurityRequirementsAndHasAtLeast512Bits")
                .compact();

        // Act
        boolean isValid = jwtUtils.validateJwtToken(tokenWithWrongSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // Arrange - Create expired token
        String expiredToken = Jwts.builder()
                .setSubject("test@yoga.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000)) // 1 second ago
                .setExpiration(new Date(System.currentTimeMillis() - 500)) // Expired 500ms ago
                .signWith(SignatureAlgorithm.HS512, testJwtSecret)
                .compact();

        // Act
        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        // Arrange
        String malformedToken = "malformed";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void generateTokenThenValidate_ShouldWorkInSequence() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(testUserDetails);

        // Act
        String token = jwtUtils.generateJwtToken(authentication);
        boolean isValid = jwtUtils.validateJwtToken(token);
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertNotNull(token);
        assertTrue(isValid);
        assertEquals("test@yoga.com", username);
    }

    @Test
    void generateTokenWithDifferentUsers_ShouldProduceDifferentTokens() {
        // Arrange
        UserDetailsImpl user1 = UserDetailsImpl.builder()
                .username("user1@yoga.com")
                .build();
        UserDetailsImpl user2 = UserDetailsImpl.builder()
                .username("user2@yoga.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(user1);
        String token1 = jwtUtils.generateJwtToken(authentication);

        when(authentication.getPrincipal()).thenReturn(user2);
        String token2 = jwtUtils.generateJwtToken(authentication);

        // Act & Assert
        assertNotEquals(token1, token2);
        
        assertEquals("user1@yoga.com", jwtUtils.getUserNameFromJwtToken(token1));
        assertEquals("user2@yoga.com", jwtUtils.getUserNameFromJwtToken(token2));
        
        assertTrue(jwtUtils.validateJwtToken(token1));
        assertTrue(jwtUtils.validateJwtToken(token2));
    }

    @Test
    void multipleTokenGenerations_ShouldAllBeValid() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(testUserDetails);

        // Act - Generate multiple tokens with some delay to ensure different timestamps
        String token1 = jwtUtils.generateJwtToken(authentication);
        try {
            Thread.sleep(1); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtUtils.generateJwtToken(authentication);
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token3 = jwtUtils.generateJwtToken(authentication);

        // Assert - All should be valid
        assertTrue(jwtUtils.validateJwtToken(token1));
        assertTrue(jwtUtils.validateJwtToken(token2));
        assertTrue(jwtUtils.validateJwtToken(token3));
        
        // All should have same username
        assertEquals("test@yoga.com", jwtUtils.getUserNameFromJwtToken(token1));
        assertEquals("test@yoga.com", jwtUtils.getUserNameFromJwtToken(token2));
        assertEquals("test@yoga.com", jwtUtils.getUserNameFromJwtToken(token3));
    }

    // ==================== HELPER METHODS ====================

    private String createValidToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + testJwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, testJwtSecret)
                .compact();
    }
}