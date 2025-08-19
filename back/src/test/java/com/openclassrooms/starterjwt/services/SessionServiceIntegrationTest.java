package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.SessionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SessionService
 * Tests the service layer with real database interactions using H2 in-memory database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SessionServiceIntegrationTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    private User testUser;
    private User testUser2;
    private Teacher testTeacher;
    private Teacher testTeacher2; // Add second teacher for unique constraint
    private Session testSession;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        teacherRepository.deleteAll();

        // Create test teachers  
        testTeacher = Teacher.builder()
                .firstName("John")
                .lastName("Yoga")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testTeacher = teacherRepository.save(testTeacher);

        testTeacher2 = Teacher.builder()
                .firstName("Jane")
                .lastName("YogaMaster")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testTeacher2 = teacherRepository.save(testTeacher2);

        // Create test users
        testUser = User.builder()
                .email("user1@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword123")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        testUser2 = User.builder()
                .email("user2@yoga.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("encodedPassword456")
                .admin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser2 = userRepository.save(testUser2);

        // Create test session
        testSession = Session.builder()
                .name("Morning Yoga")
                .description("Relaxing morning yoga session")
                .date(new Date())
                .teacher(testTeacher)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testSession = sessionRepository.save(testSession);
    }

    @Test
    void contextLoads() {
        // Verify Spring context loads correctly
        assertNotNull(sessionService);
        assertNotNull(sessionRepository);
        assertNotNull(userRepository);
        assertNotNull(teacherRepository);
    }

    // ==================== CREATE INTEGRATION TESTS ====================

    @Test
    void create_ShouldSaveSessionToDatabase_WhenSessionIsValid() {
        // Arrange
        Session newSession = Session.builder()
                .name("Evening Yoga")
                .description("Relaxing evening session")
                .date(new Date())
                .teacher(testTeacher2) // Use different teacher
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        Session savedSession = sessionService.create(newSession);

        // Assert
        assertNotNull(savedSession);
        assertNotNull(savedSession.getId());
        assertEquals("Evening Yoga", savedSession.getName());
        assertEquals("Relaxing evening session", savedSession.getDescription());

        // Verify in database
        Session foundSession = sessionRepository.findById(savedSession.getId()).orElse(null);
        assertNotNull(foundSession);
        assertEquals(savedSession.getName(), foundSession.getName());
    }

    // ==================== DELETE INTEGRATION TESTS ====================

    @Test
    void delete_ShouldRemoveSessionFromDatabase_WhenSessionExists() {
        // Arrange
        Long sessionId = testSession.getId();
        assertTrue(sessionRepository.findById(sessionId).isPresent());

        // Act
        sessionService.delete(sessionId);

        // Assert
        assertFalse(sessionRepository.findById(sessionId).isPresent());
    }

    @Test
    void delete_ShouldNotThrowException_WhenSessionDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        assertDoesNotThrow(() -> sessionService.delete(nonExistentId));
    }

    // ==================== FIND ALL INTEGRATION TESTS ====================

    @Test
    void findAll_ShouldReturnAllSessions_WhenSessionsExistInDatabase() {
        // Arrange
        Session secondSession = Session.builder()
                .name("Afternoon Yoga")
                .description("Energizing afternoon session")
                .date(new Date())
                .teacher(testTeacher2) // Use different teacher
                .build();
        sessionRepository.save(secondSession);

        // Act
        List<Session> sessions = sessionService.findAll();

        // Assert
        assertNotNull(sessions);
        assertEquals(2, sessions.size());
        assertTrue(sessions.stream().anyMatch(s -> "Morning Yoga".equals(s.getName())));
        assertTrue(sessions.stream().anyMatch(s -> "Afternoon Yoga".equals(s.getName())));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoSessionsExist() {
        // Arrange
        sessionRepository.deleteAll();

        // Act
        List<Session> sessions = sessionService.findAll();

        // Assert
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    // ==================== GET BY ID INTEGRATION TESTS ====================

    @Test
    void getById_ShouldReturnSession_WhenSessionExistsInDatabase() {
        // Act
        Session foundSession = sessionService.getById(testSession.getId());

        // Assert
        assertNotNull(foundSession);
        assertEquals(testSession.getId(), foundSession.getId());
        assertEquals(testSession.getName(), foundSession.getName());
        assertEquals(testSession.getDescription(), foundSession.getDescription());
    }

    @Test
    void getById_ShouldReturnNull_WhenSessionDoesNotExistInDatabase() {
        // Arrange
        Long nonExistentId = 999L;

        // Act
        Session foundSession = sessionService.getById(nonExistentId);

        // Assert
        assertNull(foundSession);
    }

    // ==================== UPDATE INTEGRATION TESTS ====================

    @Test
    void update_ShouldUpdateSessionInDatabase_WhenSessionExists() {
        // Arrange
        Session updatedSession = Session.builder()
                .name("Updated Session Name")
                .description("Updated description")
                .date(new Date())
                .teacher(testTeacher)
                .build();

        // Act
        Session result = sessionService.update(testSession.getId(), updatedSession);

        // Assert
        assertNotNull(result);
        assertEquals(testSession.getId(), result.getId());
        assertEquals("Updated Session Name", result.getName());
        assertEquals("Updated description", result.getDescription());

        // Verify in database
        Session foundSession = sessionRepository.findById(testSession.getId()).orElse(null);
        assertNotNull(foundSession);
        assertEquals("Updated Session Name", foundSession.getName());
        assertEquals("Updated description", foundSession.getDescription());
    }

    // ==================== PARTICIPATE INTEGRATION TESTS ====================

    @Test
    void participate_ShouldAddUserToSessionInDatabase_WhenUserAndSessionExist() {
        // Act
        sessionService.participate(testSession.getId(), testUser.getId());

        // Assert
        Session updatedSession = sessionRepository.findById(testSession.getId()).orElse(null);
        assertNotNull(updatedSession);
        assertNotNull(updatedSession.getUsers());
        assertTrue(updatedSession.getUsers().stream()
                .anyMatch(user -> user.getId().equals(testUser.getId())));
        assertEquals(1, updatedSession.getUsers().size());
    }

    @Test
    void participate_ShouldThrowNotFoundException_WhenSessionDoesNotExist() {
        // Arrange
        Long nonExistentSessionId = 999L;

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            sessionService.participate(nonExistentSessionId, testUser.getId());
        });
    }

    @Test
    void participate_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        Long nonExistentUserId = 999L;

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            sessionService.participate(testSession.getId(), nonExistentUserId);
        });
    }

    @Test
    void participate_ShouldThrowBadRequestException_WhenUserAlreadyParticipates() {
        // Arrange - User participates first time
        sessionService.participate(testSession.getId(), testUser.getId());

        // Act & Assert - Try to participate again
        assertThrows(BadRequestException.class, () -> {
            sessionService.participate(testSession.getId(), testUser.getId());
        });
    }

    @Test
    void participate_ShouldAllowMultipleUsersToParticipate() {
        // Act
        sessionService.participate(testSession.getId(), testUser.getId());
        sessionService.participate(testSession.getId(), testUser2.getId());

        // Assert
        Session updatedSession = sessionRepository.findById(testSession.getId()).orElse(null);
        assertNotNull(updatedSession);
        assertEquals(2, updatedSession.getUsers().size());
        assertTrue(updatedSession.getUsers().stream()
                .anyMatch(user -> user.getId().equals(testUser.getId())));
        assertTrue(updatedSession.getUsers().stream()
                .anyMatch(user -> user.getId().equals(testUser2.getId())));
    }

    // ==================== NO LONGER PARTICIPATE INTEGRATION TESTS ====================

    @Test
    void noLongerParticipate_ShouldRemoveUserFromSessionInDatabase_WhenUserParticipates() {
        // Arrange
        sessionService.participate(testSession.getId(), testUser.getId());
        Session sessionWithUser = sessionRepository.findById(testSession.getId()).orElse(null);
        assertEquals(1, sessionWithUser.getUsers().size());

        // Act
        sessionService.noLongerParticipate(testSession.getId(), testUser.getId());

        // Assert
        Session updatedSession = sessionRepository.findById(testSession.getId()).orElse(null);
        assertNotNull(updatedSession);
        assertEquals(0, updatedSession.getUsers().size());
    }

    @Test
    void noLongerParticipate_ShouldThrowNotFoundException_WhenSessionDoesNotExist() {
        // Arrange
        Long nonExistentSessionId = 999L;

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            sessionService.noLongerParticipate(nonExistentSessionId, testUser.getId());
        });
    }

    @Test
    void noLongerParticipate_ShouldThrowBadRequestException_WhenUserDoesNotParticipate() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            sessionService.noLongerParticipate(testSession.getId(), testUser.getId());
        });
    }

    @Test
    void noLongerParticipate_ShouldRemoveOnlySpecifiedUser_WhenMultipleUsersParticipate() {
        // Arrange
        sessionService.participate(testSession.getId(), testUser.getId());
        sessionService.participate(testSession.getId(), testUser2.getId());

        // Act
        sessionService.noLongerParticipate(testSession.getId(), testUser.getId());

        // Assert
        Session updatedSession = sessionRepository.findById(testSession.getId()).orElse(null);
        assertNotNull(updatedSession);
        assertEquals(1, updatedSession.getUsers().size());
        assertTrue(updatedSession.getUsers().stream()
                .anyMatch(user -> user.getId().equals(testUser2.getId())));
        assertFalse(updatedSession.getUsers().stream()
                .anyMatch(user -> user.getId().equals(testUser.getId())));
    }

    // ==================== COMPLEX INTEGRATION SCENARIOS ====================

    @Test
    void participateAndNoLongerParticipate_ShouldWorkInSequence() {
        // Act - Participate
        sessionService.participate(testSession.getId(), testUser.getId());
        Session sessionAfterParticipation = sessionRepository.findById(testSession.getId()).orElse(null);
        assertEquals(1, sessionAfterParticipation.getUsers().size());

        // Act - No longer participate
        sessionService.noLongerParticipate(testSession.getId(), testUser.getId());
        Session sessionAfterLeaving = sessionRepository.findById(testSession.getId()).orElse(null);

        // Assert
        assertEquals(0, sessionAfterLeaving.getUsers().size());
    }

    @Test
    void multipleOperations_ShouldMaintainDataIntegrity() {
        // Arrange - Create another session
        Session secondSession = Session.builder()
                .name("Evening Session")
                .description("Evening yoga")
                .date(new Date())
                .teacher(testTeacher2) // Use different teacher
                .build();
        secondSession = sessionRepository.save(secondSession);

        // Act - User participates in both sessions
        sessionService.participate(testSession.getId(), testUser.getId());
        sessionService.participate(secondSession.getId(), testUser.getId());

        // Act - User leaves first session only
        sessionService.noLongerParticipate(testSession.getId(), testUser.getId());

        // Assert - User should still be in second session
        Session firstSession = sessionRepository.findById(testSession.getId()).orElse(null);
        Session secondSessionUpdated = sessionRepository.findById(secondSession.getId()).orElse(null);

        assertEquals(0, firstSession.getUsers().size());
        assertEquals(1, secondSessionUpdated.getUsers().size());
        assertTrue(secondSessionUpdated.getUsers().stream()
                .anyMatch(user -> user.getId().equals(testUser.getId())));
    }

    @Test
    void sessionOperations_ShouldPersistAcrossTransactions() {
        // Create and save a new session
        Session newSession = sessionService.create(Session.builder()
                .name("Test Session")
                .description("Test description")
                .date(new Date())
                .teacher(testTeacher2) // Use different teacher
                .build());

        Long sessionId = newSession.getId();

        // Update the session
        sessionService.update(sessionId, Session.builder()
                .name("Updated Test Session")
                .description("Updated description")
                .date(new Date())
                .teacher(testTeacher)
                .build());

        // Verify persistence
        Session foundSession = sessionService.getById(sessionId);
        assertNotNull(foundSession);
        assertEquals("Updated Test Session", foundSession.getName());
        assertEquals("Updated description", foundSession.getDescription());
    }
}