package com.openclassrooms.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionService
 * Tests the business logic layer without database interaction
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    private Session testSession;
    private User testUser;
    private User testUser2;
    private Long testSessionId;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        testSessionId = 1L;
        testUserId = 1L;
        
        // Create test users
        testUser = User.builder()
                .id(testUserId)
                .email("user1@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .id(2L)
                .email("user2@yoga.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("password456")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create test session with empty users list
        testSession = Session.builder()
                .id(testSessionId)
                .name("Yoga Session")
                .description("Relaxing yoga session")
                .date(new Date())
                .users(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testServiceCreation() {
        // Verify that service is properly instantiated
        assertNotNull(sessionService);
        assertNotNull(sessionRepository);
        assertNotNull(userRepository);
    }

    // ==================== CREATE TESTS ====================

    @Test
    void create_ShouldReturnSavedSession_WhenSessionIsValid() {
        // Arrange
        when(sessionRepository.save(testSession)).thenReturn(testSession);

        // Act
        Session result = sessionService.create(testSession);

        // Assert
        assertNotNull(result);
        assertEquals(testSession.getId(), result.getId());
        assertEquals(testSession.getName(), result.getName());
        assertEquals(testSession.getDescription(), result.getDescription());

        // Verify repository interaction
        verify(sessionRepository).save(testSession);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void create_ShouldCallRepository_WhenSessionIsNull() {
        // Arrange
        when(sessionRepository.save(null)).thenReturn(null);

        // Act
        Session result = sessionService.create(null);

        // Assert
        assertNull(result);
        verify(sessionRepository).save(null);
    }

    // ==================== DELETE TESTS ====================

    @Test
    void delete_ShouldCallRepositoryDeleteById_WhenIdIsValid() {
        // Arrange
        doNothing().when(sessionRepository).deleteById(testSessionId);

        // Act
        sessionService.delete(testSessionId);

        // Assert
        verify(sessionRepository).deleteById(testSessionId);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById_WhenIdIsNull() {
        // Arrange
        doNothing().when(sessionRepository).deleteById(null);

        // Act
        sessionService.delete(null);

        // Assert
        verify(sessionRepository).deleteById(null);
        verifyNoMoreInteractions(sessionRepository);
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void findAll_ShouldReturnAllSessions_WhenSessionsExist() {
        // Arrange
        List<Session> sessions = Arrays.asList(testSession, 
            Session.builder().id(2L).name("Session 2").build());
        when(sessionRepository.findAll()).thenReturn(sessions);

        // Act
        List<Session> result = sessionService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testSession.getId(), result.get(0).getId());

        // Verify repository interaction
        verify(sessionRepository).findAll();
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoSessionsExist() {
        // Arrange
        when(sessionRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Session> result = sessionService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sessionRepository).findAll();
    }

    // ==================== GET BY ID TESTS ====================

    @Test
    void getById_ShouldReturnSession_WhenSessionExists() {
        // Arrange
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));

        // Act
        Session result = sessionService.getById(testSessionId);

        // Assert
        assertNotNull(result);
        assertEquals(testSession.getId(), result.getId());
        assertEquals(testSession.getName(), result.getName());

        // Verify repository interaction
        verify(sessionRepository).findById(testSessionId);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void getById_ShouldReturnNull_WhenSessionDoesNotExist() {
        // Arrange
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.empty());

        // Act
        Session result = sessionService.getById(testSessionId);

        // Assert
        assertNull(result);
        verify(sessionRepository).findById(testSessionId);
    }

    @Test
    void getById_ShouldReturnNull_WhenIdIsNull() {
        // Arrange
        when(sessionRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Session result = sessionService.getById(null);

        // Assert
        assertNull(result);
        verify(sessionRepository).findById(null);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void update_ShouldSetIdAndReturnUpdatedSession_WhenSessionIsValid() {
        // Arrange
        Session sessionToUpdate = Session.builder()
                .name("Updated Session")
                .description("Updated description")
                .build();
        
        Session expectedSession = Session.builder()
                .id(testSessionId)
                .name("Updated Session")
                .description("Updated description")
                .build();

        when(sessionRepository.save(any(Session.class))).thenReturn(expectedSession);

        // Act
        Session result = sessionService.update(testSessionId, sessionToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(testSessionId, result.getId());
        assertEquals("Updated Session", result.getName());
        assertEquals("Updated description", result.getDescription());

        // Verify repository interaction
        verify(sessionRepository).save(any(Session.class));
        verifyNoMoreInteractions(sessionRepository);
    }

    // ==================== PARTICIPATE TESTS ====================

    @Test
    void participate_ShouldAddUserToSession_WhenUserAndSessionExist() {
        // Arrange
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(sessionRepository.save(testSession)).thenReturn(testSession);

        // Act
        sessionService.participate(testSessionId, testUserId);

        // Assert
        assertTrue(testSession.getUsers().contains(testUser));
        verify(sessionRepository).findById(testSessionId);
        verify(userRepository).findById(testUserId);
        verify(sessionRepository).save(testSession);
    }

    @Test
    void participate_ShouldThrowNotFoundException_WhenSessionDoesNotExist() {
        // Arrange
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.empty());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            sessionService.participate(testSessionId, testUserId);
        });

        verify(sessionRepository).findById(testSessionId);
        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void participate_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            sessionService.participate(testSessionId, testUserId);
        });

        verify(sessionRepository).findById(testSessionId);
        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void participate_ShouldThrowBadRequestException_WhenUserAlreadyParticipates() {
        // Arrange
        testSession.getUsers().add(testUser); // User already participates
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            sessionService.participate(testSessionId, testUserId);
        });

        verify(sessionRepository).findById(testSessionId);
        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(sessionRepository);
    }

    // ==================== NO LONGER PARTICIPATE TESTS ====================

    @Test
    void noLongerParticipate_ShouldRemoveUserFromSession_WhenUserParticipates() {
        // Arrange
        testSession.getUsers().add(testUser); // User participates
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(testSession)).thenReturn(testSession);

        // Act
        sessionService.noLongerParticipate(testSessionId, testUserId);

        // Assert
        assertFalse(testSession.getUsers().contains(testUser));
        verify(sessionRepository).findById(testSessionId);
        verify(sessionRepository).save(testSession);
    }

    @Test
    void noLongerParticipate_ShouldThrowNotFoundException_WhenSessionDoesNotExist() {
        // Arrange
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            sessionService.noLongerParticipate(testSessionId, testUserId);
        });

        verify(sessionRepository).findById(testSessionId);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void noLongerParticipate_ShouldThrowBadRequestException_WhenUserDoesNotParticipate() {
        // Arrange
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            sessionService.noLongerParticipate(testSessionId, testUserId);
        });

        verify(sessionRepository).findById(testSessionId);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    void noLongerParticipate_ShouldRemoveOnlySpecifiedUser_WhenMultipleUsersParticipate() {
        // Arrange
        testSession.getUsers().add(testUser);
        testSession.getUsers().add(testUser2);
        when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(testSession)).thenReturn(testSession);

        // Act
        sessionService.noLongerParticipate(testSessionId, testUserId);

        // Assert
        assertFalse(testSession.getUsers().contains(testUser));
        assertTrue(testSession.getUsers().contains(testUser2));
        assertEquals(1, testSession.getUsers().size());
        
        verify(sessionRepository).findById(testSessionId);
        verify(sessionRepository).save(testSession);
    }
}