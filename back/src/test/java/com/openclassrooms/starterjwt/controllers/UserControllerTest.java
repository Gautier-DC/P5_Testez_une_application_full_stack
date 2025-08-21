package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController
 * Tests the REST API layer with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDto testUserDto;
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

        testUserDto = new UserDto();
        testUserDto.setId(testUserId);
        testUserDto.setEmail("test@yoga.com");
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setAdmin(false);
    }

    @Test
    void testControllerCreation() {
        assertNotNull(userController);
        assertNotNull(userService);
        assertNotNull(userMapper);
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    void findById_ShouldReturnUserDto_WhenValidIdAndUserExists() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // Act
        ResponseEntity<?> response = userController.findById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserDto, response.getBody());

        verify(userService).findById(1L);
        verify(userMapper).toDto(testUser);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenValidIdButUserDoesNotExist() {
        // Arrange
        String id = "999";
        when(userService.findById(999L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.findById(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(userService).findById(999L);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";

        // Act
        ResponseEntity<?> response = userController.findById(invalidId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(userService);
        verifyNoInteractions(userMapper);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsEmpty() {
        // Arrange
        String emptyId = "";

        // Act
        ResponseEntity<?> response = userController.findById(emptyId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(userService);
        verifyNoInteractions(userMapper);
    }

    @Test
    void findById_ShouldHandleNegativeId_WhenServiceReturnsNull() {
        // Arrange
        String negativeId = "-1";
        when(userService.findById(-1L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.findById(negativeId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(userService).findById(-1L);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void findById_ShouldHandleZeroId_WhenServiceReturnsNull() {
        // Arrange
        String zeroId = "0";
        when(userService.findById(0L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.findById(zeroId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(userService).findById(0L);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void findById_ShouldHandleLargeId_WhenServiceReturnsNull() {
        // Arrange
        String largeId = String.valueOf(Long.MAX_VALUE);
        when(userService.findById(Long.MAX_VALUE)).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.findById(largeId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(userService).findById(Long.MAX_VALUE);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userService);
    }

    // ==================== DELETE TESTS ====================

    @Test
    void save_ShouldReturnOk_WhenValidIdAndUserExistsAndIsAuthorized() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("test@yoga.com");
            doNothing().when(userService).delete(1L);

            // Act
            ResponseEntity<?> response = userController.save(id);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody());

            verify(userService).findById(1L);
            verify(userService).delete(1L);
            verifyNoMoreInteractions(userService);
        }
    }

    @Test
    void save_ShouldReturnNotFound_WhenValidIdButUserDoesNotExist() {
        // Arrange
        String id = "999";
        when(userService.findById(999L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.save(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(userService).findById(999L);
        verify(userService, never()).delete(anyLong());
    }

    @Test
    void save_ShouldReturnUnauthorized_WhenUserExistsButNotAuthorized() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("other@yoga.com"); // Different email

            // Act
            ResponseEntity<?> response = userController.save(id);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());

            verify(userService).findById(1L);
            verify(userService, never()).delete(anyLong());
        }
    }

    @Test
    void save_ShouldReturnBadRequest_WhenIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";

        // Act
        ResponseEntity<?> response = userController.save(invalidId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(userService);
    }

    @Test
    void save_ShouldReturnBadRequest_WhenIdIsEmpty() {
        // Arrange
        String emptyId = "";

        // Act
        ResponseEntity<?> response = userController.save(emptyId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(userService);
    }

    @Test
    void save_ShouldHandleNullUserDetails() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                userController.save(id);
            });

            verify(userService).findById(1L);
            verify(userService, never()).delete(anyLong());
        }
    }

    @Test
    void save_ShouldHandleNullAuthentication() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                userController.save(id);
            });

            verify(userService).findById(1L);
            verify(userService, never()).delete(anyLong());
        }
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void findById_ShouldHandleServiceException() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userController.findById(id);
        });

        verify(userService).findById(1L);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void findById_ShouldHandleMapperException() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userController.findById(id);
        });

        verify(userService).findById(1L);
        verify(userMapper).toDto(testUser);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    void save_ShouldHandleServiceException() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("test@yoga.com");
            doThrow(new RuntimeException("Database error")).when(userService).delete(1L);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                userController.save(id);
            });

            verify(userService).findById(1L);
            verify(userService).delete(1L);
        }
    }

    // ==================== INTEGRATION SCENARIOS ====================

    @Test
    void findByIdThenDelete_ShouldWorkInSequence() {
        // Arrange
        String id = "1";
        when(userService.findById(1L)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("test@yoga.com");
            doNothing().when(userService).delete(1L);

            // Act
            ResponseEntity<?> findResponse = userController.findById(id);
            ResponseEntity<?> deleteResponse = userController.save(id);

            // Assert
            assertEquals(HttpStatus.OK, findResponse.getStatusCode());
            assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
            assertEquals(testUserDto, findResponse.getBody());
            assertNull(deleteResponse.getBody());

            verify(userService, times(2)).findById(1L); // Called twice
            verify(userService).delete(1L);
            verify(userMapper).toDto(testUser);
        }
    }

    @Test
    void multipleRequests_ShouldWorkIndependently() {
        // Arrange
        when(userService.findById(1L)).thenReturn(testUser);
        when(userService.findById(2L)).thenReturn(null);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // Act
        ResponseEntity<?> response1 = userController.findById("1");
        ResponseEntity<?> response2 = userController.findById("2");

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(testUserDto, response1.getBody());
        
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
        assertNull(response2.getBody());

        verify(userService).findById(1L);
        verify(userService).findById(2L);
        verify(userMapper).toDto(testUser);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(userMapper);
    }
}