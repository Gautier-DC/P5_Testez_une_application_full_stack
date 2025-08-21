package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeacherController
 * Tests the REST API layer with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class TeacherControllerTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherController teacherController;

    private Teacher testTeacher;
    private TeacherDto testTeacherDto;
    private Long testTeacherId;

    @BeforeEach
    void setUp() {
        testTeacherId = 1L;
        
        testTeacher = Teacher.builder()
                .id(testTeacherId)
                .firstName("John")
                .lastName("YogaMaster")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTeacherDto = new TeacherDto();
        testTeacherDto.setId(testTeacherId);
        testTeacherDto.setFirstName("John");
        testTeacherDto.setLastName("YogaMaster");
    }

    @Test
    void testControllerCreation() {
        // Verify that controller is properly instantiated
        assertNotNull(teacherController);
        assertNotNull(teacherService);
        assertNotNull(teacherMapper);
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    void findById_ShouldReturnTeacherDto_WhenValidIdAndTeacherExists() {
        // Arrange
        String id = "1";
        when(teacherService.findById(1L)).thenReturn(testTeacher);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);

        // Act
        ResponseEntity<?> response = teacherController.findById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTeacherDto, response.getBody());

        // Verify interactions
        verify(teacherService).findById(1L);
        verify(teacherMapper).toDto(testTeacher);
        verifyNoMoreInteractions(teacherService);
        verifyNoMoreInteractions(teacherMapper);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenValidIdButTeacherDoesNotExist() {
        // Arrange
        String id = "999";
        when(teacherService.findById(999L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = teacherController.findById(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify interactions
        verify(teacherService).findById(999L);
        verifyNoInteractions(teacherMapper);
        verifyNoMoreInteractions(teacherService);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsNotNumeric() {
        // Arrange
        String invalidId = "not-a-number";

        // Act
        ResponseEntity<?> response = teacherController.findById(invalidId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        // Verify no service interactions for invalid input
        verifyNoInteractions(teacherService);
        verifyNoInteractions(teacherMapper);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsEmpty() {
        // Arrange
        String emptyId = "";

        // Act
        ResponseEntity<?> response = teacherController.findById(emptyId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        // Verify no service interactions
        verifyNoInteractions(teacherService);
        verifyNoInteractions(teacherMapper);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdContainsNonNumericCharacters() {
        // Arrange
        String invalidId = "1a";

        // Act
        ResponseEntity<?> response = teacherController.findById(invalidId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        // Verify no service interactions
        verifyNoInteractions(teacherService);
        verifyNoInteractions(teacherMapper);
    }

    @Test
    void findById_ShouldHandleNegativeId_WhenServiceReturnsNull() {
        // Arrange
        String negativeId = "-1";
        when(teacherService.findById(-1L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = teacherController.findById(negativeId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify interactions
        verify(teacherService).findById(-1L);
        verifyNoInteractions(teacherMapper);
        verifyNoMoreInteractions(teacherService);
    }

    @Test
    void findById_ShouldHandleZeroId_WhenServiceReturnsNull() {
        // Arrange
        String zeroId = "0";
        when(teacherService.findById(0L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = teacherController.findById(zeroId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify interactions
        verify(teacherService).findById(0L);
        verifyNoInteractions(teacherMapper);
        verifyNoMoreInteractions(teacherService);
    }

    @Test
    void findById_ShouldHandleLargeId_WhenServiceReturnsNull() {
        // Arrange
        String largeId = String.valueOf(Long.MAX_VALUE);
        when(teacherService.findById(Long.MAX_VALUE)).thenReturn(null);

        // Act
        ResponseEntity<?> response = teacherController.findById(largeId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify interactions
        verify(teacherService).findById(Long.MAX_VALUE);
        verifyNoInteractions(teacherMapper);
        verifyNoMoreInteractions(teacherService);
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void findAll_ShouldReturnListOfTeacherDtos_WhenTeachersExist() {
        // Arrange
        Teacher testTeacher2 = Teacher.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("YogaExpert")
                .build();

        List<Teacher> teachers = Arrays.asList(testTeacher, testTeacher2);
        
        TeacherDto testTeacherDto2 = new TeacherDto();
        testTeacherDto2.setId(2L);
        testTeacherDto2.setFirstName("Jane");
        testTeacherDto2.setLastName("YogaExpert");
        
        List<TeacherDto> teacherDtos = Arrays.asList(testTeacherDto, testTeacherDto2);

        when(teacherService.findAll()).thenReturn(teachers);
        when(teacherMapper.toDto(teachers)).thenReturn(teacherDtos);

        // Act
        ResponseEntity<?> response = teacherController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(teacherDtos, response.getBody());

        // Verify the response body is a list with correct size
        @SuppressWarnings("unchecked")
        List<TeacherDto> responseBody = (List<TeacherDto>) response.getBody();
        assertEquals(2, responseBody.size());
        assertEquals(testTeacherDto, responseBody.get(0));
        assertEquals(testTeacherDto2, responseBody.get(1));

        // Verify interactions
        verify(teacherService).findAll();
        verify(teacherMapper).toDto(teachers);
        verifyNoMoreInteractions(teacherService);
        verifyNoMoreInteractions(teacherMapper);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoTeachersExist() {
        // Arrange
        List<Teacher> emptyTeachers = Arrays.asList();
        List<TeacherDto> emptyTeacherDtos = Arrays.asList();

        when(teacherService.findAll()).thenReturn(emptyTeachers);
        when(teacherMapper.toDto(emptyTeachers)).thenReturn(emptyTeacherDtos);

        // Act
        ResponseEntity<?> response = teacherController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyTeacherDtos, response.getBody());

        // Verify the response body is an empty list
        @SuppressWarnings("unchecked")
        List<TeacherDto> responseBody = (List<TeacherDto>) response.getBody();
        assertTrue(responseBody.isEmpty());

        // Verify interactions
        verify(teacherService).findAll();
        verify(teacherMapper).toDto(emptyTeachers);
        verifyNoMoreInteractions(teacherService);
        verifyNoMoreInteractions(teacherMapper);
    }

    @Test
    void findAll_ShouldReturnSingleTeacher_WhenOnlyOneTeacherExists() {
        // Arrange
        List<Teacher> singleTeacher = Arrays.asList(testTeacher);
        List<TeacherDto> singleTeacherDto = Arrays.asList(testTeacherDto);

        when(teacherService.findAll()).thenReturn(singleTeacher);
        when(teacherMapper.toDto(singleTeacher)).thenReturn(singleTeacherDto);

        // Act
        ResponseEntity<?> response = teacherController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(singleTeacherDto, response.getBody());

        // Verify the response body is a list with one item
        @SuppressWarnings("unchecked")
        List<TeacherDto> responseBody = (List<TeacherDto>) response.getBody();
        assertEquals(1, responseBody.size());
        assertEquals(testTeacherDto, responseBody.get(0));

        // Verify interactions
        verify(teacherService).findAll();
        verify(teacherMapper).toDto(singleTeacher);
        verifyNoMoreInteractions(teacherService);
        verifyNoMoreInteractions(teacherMapper);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void findById_ShouldHandleServiceException() {
        // Arrange
        String id = "1";
        when(teacherService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            teacherController.findById(id);
        });

        // Verify interactions
        verify(teacherService).findById(1L);
        verifyNoInteractions(teacherMapper);
        verifyNoMoreInteractions(teacherService);
    }

    @Test
    void findAll_ShouldHandleServiceException() {
        // Arrange
        when(teacherService.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            teacherController.findAll();
        });

        // Verify interactions
        verify(teacherService).findAll();
        verifyNoInteractions(teacherMapper);
        verifyNoMoreInteractions(teacherService);
    }

    @Test
    void findById_ShouldHandleMapperException() {
        // Arrange
        String id = "1";
        when(teacherService.findById(1L)).thenReturn(testTeacher);
        when(teacherMapper.toDto(testTeacher)).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            teacherController.findById(id);
        });

        // Verify interactions
        verify(teacherService).findById(1L);
        verify(teacherMapper).toDto(testTeacher);
        verifyNoMoreInteractions(teacherService);
        verifyNoMoreInteractions(teacherMapper);
    }

    @Test
    void findAll_ShouldHandleMapperException() {
        // Arrange
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherService.findAll()).thenReturn(teachers);
        when(teacherMapper.toDto(anyList())).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            teacherController.findAll();
        });

        // Verify interactions
        verify(teacherService).findAll();
        verify(teacherMapper).toDto(teachers);
        verifyNoMoreInteractions(teacherService);
        verifyNoMoreInteractions(teacherMapper);
    }

    // ==================== INTEGRATION SCENARIOS ====================

    @Test
    void multipleRequests_ShouldWorkIndependently() {
        // Arrange
        when(teacherService.findById(1L)).thenReturn(testTeacher);
        when(teacherMapper.toDto(testTeacher)).thenReturn(testTeacherDto);
        
        List<Teacher> teachers = Arrays.asList(testTeacher);
        List<TeacherDto> teacherDtos = Arrays.asList(testTeacherDto);
        when(teacherService.findAll()).thenReturn(teachers);
        when(teacherMapper.toDto(teachers)).thenReturn(teacherDtos);

        // Act
        ResponseEntity<?> response1 = teacherController.findById("1");
        ResponseEntity<?> response2 = teacherController.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(testTeacherDto, response1.getBody());
        
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(teacherDtos, response2.getBody());

        // Verify interactions
        verify(teacherService).findById(1L);
        verify(teacherService).findAll();
        verify(teacherMapper).toDto(testTeacher);
        verify(teacherMapper).toDto(teachers);
        verifyNoMoreInteractions(teacherService);
        verifyNoMoreInteractions(teacherMapper);
    }
}