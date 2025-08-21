package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionController
 * Tests the REST API layer with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionController sessionController;

    private Session testSession;
    private SessionDto testSessionDto;
    private Long testSessionId;

    @BeforeEach
    void setUp() {
        testSessionId = 1L;
        
        testSession = Session.builder()
                .id(testSessionId)
                .name("Yoga Session")
                .description("A relaxing yoga session")
                .date(new Date())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testSessionDto = new SessionDto();
        testSessionDto.setId(testSessionId);
        testSessionDto.setName("Yoga Session");
        testSessionDto.setDescription("A relaxing yoga session");
        testSessionDto.setDate(new Date());
    }

    @Test
    void testControllerCreation() {
        assertNotNull(sessionController);
        assertNotNull(sessionService);
        assertNotNull(sessionMapper);
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    void findById_ShouldReturnSessionDto_WhenValidIdAndSessionExists() {
        // Arrange
        String id = "1";
        when(sessionService.getById(1L)).thenReturn(testSession);
        when(sessionMapper.toDto(testSession)).thenReturn(testSessionDto);

        // Act
        ResponseEntity<?> response = sessionController.findById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSessionDto, response.getBody());

        verify(sessionService).getById(1L);
        verify(sessionMapper).toDto(testSession);
        verifyNoMoreInteractions(sessionService);
        verifyNoMoreInteractions(sessionMapper);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenValidIdButSessionDoesNotExist() {
        // Arrange
        String id = "999";
        when(sessionService.getById(999L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = sessionController.findById(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(sessionService).getById(999L);
        verifyNoInteractions(sessionMapper);
        verifyNoMoreInteractions(sessionService);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";

        // Act
        ResponseEntity<?> response = sessionController.findById(invalidId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
        verifyNoInteractions(sessionMapper);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsEmpty() {
        // Arrange
        String emptyId = "";

        // Act
        ResponseEntity<?> response = sessionController.findById(emptyId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
        verifyNoInteractions(sessionMapper);
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void findAll_ShouldReturnListOfSessionDtos_WhenSessionsExist() {
        // Arrange
        Session testSession2 = Session.builder()
                .id(2L)
                .name("Advanced Yoga")
                .description("Advanced yoga session")
                .build();

        List<Session> sessions = Arrays.asList(testSession, testSession2);
        
        SessionDto testSessionDto2 = new SessionDto();
        testSessionDto2.setId(2L);
        testSessionDto2.setName("Advanced Yoga");
        testSessionDto2.setDescription("Advanced yoga session");
        
        List<SessionDto> sessionDtos = Arrays.asList(testSessionDto, testSessionDto2);

        when(sessionService.findAll()).thenReturn(sessions);
        when(sessionMapper.toDto(sessions)).thenReturn(sessionDtos);

        // Act
        ResponseEntity<?> response = sessionController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sessionDtos, response.getBody());

        @SuppressWarnings("unchecked")
        List<SessionDto> responseBody = (List<SessionDto>) response.getBody();
        assertEquals(2, responseBody.size());
        assertEquals(testSessionDto, responseBody.get(0));
        assertEquals(testSessionDto2, responseBody.get(1));

        verify(sessionService).findAll();
        verify(sessionMapper).toDto(sessions);
        verifyNoMoreInteractions(sessionService);
        verifyNoMoreInteractions(sessionMapper);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoSessionsExist() {
        // Arrange
        List<Session> emptySessions = Arrays.asList();
        List<SessionDto> emptySessionDtos = Arrays.asList();

        when(sessionService.findAll()).thenReturn(emptySessions);
        when(sessionMapper.toDto(emptySessions)).thenReturn(emptySessionDtos);

        // Act
        ResponseEntity<?> response = sessionController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptySessionDtos, response.getBody());

        @SuppressWarnings("unchecked")
        List<SessionDto> responseBody = (List<SessionDto>) response.getBody();
        assertTrue(responseBody.isEmpty());

        verify(sessionService).findAll();
        verify(sessionMapper).toDto(emptySessions);
        verifyNoMoreInteractions(sessionService);
        verifyNoMoreInteractions(sessionMapper);
    }

    // ==================== CREATE TESTS ====================

    @Test
    void create_ShouldReturnCreatedSessionDto_WhenValidSessionDto() {
        // Arrange
        Session createdSession = Session.builder()
                .id(2L)
                .name("New Yoga Session")
                .description("A new yoga session")
                .build();

        SessionDto createdSessionDto = new SessionDto();
        createdSessionDto.setId(2L);
        createdSessionDto.setName("New Yoga Session");
        createdSessionDto.setDescription("A new yoga session");

        when(sessionMapper.toEntity(testSessionDto)).thenReturn(testSession);
        when(sessionService.create(testSession)).thenReturn(createdSession);
        when(sessionMapper.toDto(createdSession)).thenReturn(createdSessionDto);

        // Act
        ResponseEntity<?> response = sessionController.create(testSessionDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdSessionDto, response.getBody());

        verify(sessionMapper).toEntity(testSessionDto);
        verify(sessionService).create(testSession);
        verify(sessionMapper).toDto(createdSession);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void update_ShouldReturnUpdatedSessionDto_WhenValidIdAndSessionDto() {
        // Arrange
        String id = "1";
        Session updatedSession = Session.builder()
                .id(1L)
                .name("Updated Yoga Session")
                .description("An updated yoga session")
                .build();

        SessionDto updatedSessionDto = new SessionDto();
        updatedSessionDto.setId(1L);
        updatedSessionDto.setName("Updated Yoga Session");
        updatedSessionDto.setDescription("An updated yoga session");

        when(sessionMapper.toEntity(testSessionDto)).thenReturn(testSession);
        when(sessionService.update(1L, testSession)).thenReturn(updatedSession);
        when(sessionMapper.toDto(updatedSession)).thenReturn(updatedSessionDto);

        // Act
        ResponseEntity<?> response = sessionController.update(id, testSessionDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSessionDto, response.getBody());

        verify(sessionMapper).toEntity(testSessionDto);
        verify(sessionService).update(1L, testSession);
        verify(sessionMapper).toDto(updatedSession);
    }

    @Test
    void update_ShouldReturnBadRequest_WhenIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";

        // Act
        ResponseEntity<?> response = sessionController.update(invalidId, testSessionDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
        verifyNoInteractions(sessionMapper);
    }

    // ==================== DELETE TESTS ====================

    @Test
    void save_ShouldReturnOk_WhenValidIdAndSessionExists() {
        // Arrange
        String id = "1";
        when(sessionService.getById(1L)).thenReturn(testSession);
        doNothing().when(sessionService).delete(1L);

        // Act
        ResponseEntity<?> response = sessionController.save(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(sessionService).getById(1L);
        verify(sessionService).delete(1L);
        verifyNoMoreInteractions(sessionService);
    }

    @Test
    void save_ShouldReturnNotFound_WhenValidIdButSessionDoesNotExist() {
        // Arrange
        String id = "999";
        when(sessionService.getById(999L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = sessionController.save(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(sessionService).getById(999L);
        verify(sessionService, never()).delete(anyLong());
    }

    @Test
    void save_ShouldReturnBadRequest_WhenIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";

        // Act
        ResponseEntity<?> response = sessionController.save(invalidId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
    }

    // ==================== PARTICIPATE TESTS ====================

    @Test
    void participate_ShouldReturnOk_WhenValidIds() {
        // Arrange
        String id = "1";
        String userId = "2";
        doNothing().when(sessionService).participate(1L, 2L);

        // Act
        ResponseEntity<?> response = sessionController.participate(id, userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(sessionService).participate(1L, 2L);
        verifyNoMoreInteractions(sessionService);
    }

    @Test
    void participate_ShouldReturnBadRequest_WhenSessionIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";
        String userId = "2";

        // Act
        ResponseEntity<?> response = sessionController.participate(invalidId, userId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
    }

    @Test
    void participate_ShouldReturnBadRequest_WhenUserIdIsNotNumeric() {
        // Arrange
        String id = "1";
        String invalidUserId = "not-a-number";

        // Act
        ResponseEntity<?> response = sessionController.participate(id, invalidUserId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
    }

    // ==================== NO LONGER PARTICIPATE TESTS ====================

    @Test
    void noLongerParticipate_ShouldReturnOk_WhenValidIds() {
        // Arrange
        String id = "1";
        String userId = "2";
        doNothing().when(sessionService).noLongerParticipate(1L, 2L);

        // Act
        ResponseEntity<?> response = sessionController.noLongerParticipate(id, userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(sessionService).noLongerParticipate(1L, 2L);
        verifyNoMoreInteractions(sessionService);
    }

    @Test
    void noLongerParticipate_ShouldReturnBadRequest_WhenSessionIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";
        String userId = "2";

        // Act
        ResponseEntity<?> response = sessionController.noLongerParticipate(invalidId, userId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
    }

    @Test
    void noLongerParticipate_ShouldReturnBadRequest_WhenUserIdIsNotNumeric() {
        // Arrange
        String id = "1";
        String invalidUserId = "not-a-number";

        // Act
        ResponseEntity<?> response = sessionController.noLongerParticipate(id, invalidUserId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(sessionService);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void findById_ShouldHandleServiceException() {
        // Arrange
        String id = "1";
        when(sessionService.getById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            sessionController.findById(id);
        });

        verify(sessionService).getById(1L);
        verifyNoInteractions(sessionMapper);
    }

    @Test
    void create_ShouldHandleServiceException() {
        // Arrange
        when(sessionMapper.toEntity(testSessionDto)).thenReturn(testSession);
        when(sessionService.create(testSession)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            sessionController.create(testSessionDto);
        });

        verify(sessionMapper).toEntity(testSessionDto);
        verify(sessionService).create(testSession);
    }

    @Test
    void participate_ShouldHandleServiceException() {
        // Arrange
        String id = "1";
        String userId = "2";
        doThrow(new RuntimeException("Service error")).when(sessionService).participate(1L, 2L);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            sessionController.participate(id, userId);
        });

        verify(sessionService).participate(1L, 2L);
    }

    // ==================== INTEGRATION SCENARIOS ====================

    @Test
    void createThenFindById_ShouldWorkInSequence() {
        // Arrange - Create
        Session createdSession = Session.builder()
                .id(2L)
                .name("New Session")
                .build();
        SessionDto createdSessionDto = new SessionDto();
        createdSessionDto.setId(2L);
        createdSessionDto.setName("New Session");

        when(sessionMapper.toEntity(testSessionDto)).thenReturn(testSession);
        when(sessionService.create(testSession)).thenReturn(createdSession);
        when(sessionMapper.toDto(createdSession)).thenReturn(createdSessionDto);

        // Arrange - Find by ID
        when(sessionService.getById(2L)).thenReturn(createdSession);
        when(sessionMapper.toDto(createdSession)).thenReturn(createdSessionDto);

        // Act
        ResponseEntity<?> createResponse = sessionController.create(testSessionDto);
        ResponseEntity<?> findResponse = sessionController.findById("2");

        // Assert
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertEquals(HttpStatus.OK, findResponse.getStatusCode());
        assertEquals(createdSessionDto, createResponse.getBody());
        assertEquals(createdSessionDto, findResponse.getBody());
    }

    @Test
    void updateThenDelete_ShouldWorkInSequence() {
        // Arrange - Update
        Session updatedSession = Session.builder()
                .id(1L)
                .name("Updated Session")
                .build();
        SessionDto updatedSessionDto = new SessionDto();
        updatedSessionDto.setId(1L);
        updatedSessionDto.setName("Updated Session");

        when(sessionMapper.toEntity(testSessionDto)).thenReturn(testSession);
        when(sessionService.update(1L, testSession)).thenReturn(updatedSession);
        when(sessionMapper.toDto(updatedSession)).thenReturn(updatedSessionDto);

        // Arrange - Delete
        when(sessionService.getById(1L)).thenReturn(updatedSession);
        doNothing().when(sessionService).delete(1L);

        // Act
        ResponseEntity<?> updateResponse = sessionController.update("1", testSessionDto);
        ResponseEntity<?> deleteResponse = sessionController.save("1");

        // Assert
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertEquals(updatedSessionDto, updateResponse.getBody());
        assertNull(deleteResponse.getBody());
    }
}