package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.services.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeacherService
 * Tests the business logic layer without database interaction
 */
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher testTeacher;
    private Teacher testTeacher2;
    private Long testTeacherId;

    @BeforeEach
    void setUp() {
        testTeacherId = 1L;
        
        // Create test teachers
        testTeacher = Teacher.builder()
                .id(testTeacherId)
                .firstName("John")
                .lastName("YogaMaster")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTeacher2 = Teacher.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("YogaExpert")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testServiceCreation() {
        // Verify that service is properly instantiated
        assertNotNull(teacherService);
        assertNotNull(teacherRepository);
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    void findAll_ShouldReturnAllTeachers_WhenTeachersExist() {
        // Arrange
        List<Teacher> teachers = Arrays.asList(testTeacher, testTeacher2);
        when(teacherRepository.findAll()).thenReturn(teachers);

        // Act
        List<Teacher> result = teacherService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testTeacher.getId(), result.get(0).getId());
        assertEquals(testTeacher.getFirstName(), result.get(0).getFirstName());
        assertEquals(testTeacher.getLastName(), result.get(0).getLastName());
        assertEquals(testTeacher2.getId(), result.get(1).getId());

        // Verify repository interaction
        verify(teacherRepository).findAll();
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoTeachersExist() {
        // Arrange
        when(teacherRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Teacher> result = teacherService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());

        // Verify repository interaction
        verify(teacherRepository).findAll();
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findAll_ShouldReturnSingleTeacher_WhenOnlyOneTeacherExists() {
        // Arrange
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherRepository.findAll()).thenReturn(teachers);

        // Act
        List<Teacher> result = teacherService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTeacher.getId(), result.get(0).getId());
        assertEquals(testTeacher.getFirstName(), result.get(0).getFirstName());
        assertEquals(testTeacher.getLastName(), result.get(0).getLastName());

        // Verify repository interaction
        verify(teacherRepository).findAll();
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findAll_ShouldHandleRepositoryException() {
        // Arrange
        when(teacherRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            teacherService.findAll();
        });

        // Verify repository interaction
        verify(teacherRepository).findAll();
        verifyNoMoreInteractions(teacherRepository);
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    void findById_ShouldReturnTeacher_WhenTeacherExists() {
        // Arrange
        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(testTeacher));

        // Act
        Teacher result = teacherService.findById(testTeacherId);

        // Assert
        assertNotNull(result);
        assertEquals(testTeacher.getId(), result.getId());
        assertEquals(testTeacher.getFirstName(), result.getFirstName());
        assertEquals(testTeacher.getLastName(), result.getLastName());
        assertEquals(testTeacher.getCreatedAt(), result.getCreatedAt());
        assertEquals(testTeacher.getUpdatedAt(), result.getUpdatedAt());

        // Verify repository interaction
        verify(teacherRepository).findById(testTeacherId);
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findById_ShouldReturnNull_WhenTeacherDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;
        when(teacherRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Teacher result = teacherService.findById(nonExistentId);

        // Assert
        assertNull(result);

        // Verify repository interaction
        verify(teacherRepository).findById(nonExistentId);
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findById_ShouldReturnNull_WhenIdIsNull() {
        // Arrange
        when(teacherRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Teacher result = teacherService.findById(null);

        // Assert
        assertNull(result);

        // Verify repository interaction
        verify(teacherRepository).findById(null);
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findById_ShouldHandleRepositoryException() {
        // Arrange
        when(teacherRepository.findById(testTeacherId)).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            teacherService.findById(testTeacherId);
        });

        // Verify repository interaction
        verify(teacherRepository).findById(testTeacherId);
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findById_ShouldReturnCorrectTeacher_WhenMultipleTeachersExist() {
        // Arrange - Multiple teachers exist, but we want specific one
        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(testTeacher2));

        // Act
        Teacher result1 = teacherService.findById(testTeacherId);
        Teacher result2 = teacherService.findById(2L);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());
        assertEquals("John", result1.getFirstName());
        assertEquals("Jane", result2.getFirstName());
        assertEquals("YogaMaster", result1.getLastName());
        assertEquals("YogaExpert", result2.getLastName());

        // Verify repository interactions
        verify(teacherRepository).findById(testTeacherId);
        verify(teacherRepository).findById(2L);
        verifyNoMoreInteractions(teacherRepository);
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    void findById_ShouldHandleLargeId() {
        // Arrange
        Long largeId = Long.MAX_VALUE;
        when(teacherRepository.findById(largeId)).thenReturn(Optional.empty());

        // Act
        Teacher result = teacherService.findById(largeId);

        // Assert
        assertNull(result);

        // Verify repository interaction
        verify(teacherRepository).findById(largeId);
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findById_ShouldHandleNegativeId() {
        // Arrange
        Long negativeId = -1L;
        when(teacherRepository.findById(negativeId)).thenReturn(Optional.empty());

        // Act
        Teacher result = teacherService.findById(negativeId);

        // Assert
        assertNull(result);

        // Verify repository interaction
        verify(teacherRepository).findById(negativeId);
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void findById_ShouldHandleZeroId() {
        // Arrange
        Long zeroId = 0L;
        when(teacherRepository.findById(zeroId)).thenReturn(Optional.empty());

        // Act
        Teacher result = teacherService.findById(zeroId);

        // Assert
        assertNull(result);

        // Verify repository interaction
        verify(teacherRepository).findById(zeroId);
        verifyNoMoreInteractions(teacherRepository);
    }

    // ==================== MULTIPLE CALLS TESTS ====================

    @Test
    void multipleFindAllCalls_ShouldEachCallRepository() {
        // Arrange
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherRepository.findAll()).thenReturn(teachers);

        // Act
        List<Teacher> result1 = teacherService.findAll();
        List<Teacher> result2 = teacherService.findAll();

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(result1.get(0).getId(), result2.get(0).getId());

        // Verify repository was called twice
        verify(teacherRepository, times(2)).findAll();
        verifyNoMoreInteractions(teacherRepository);
    }

    @Test
    void multipleFindByIdCalls_ShouldEachCallRepository() {
        // Arrange
        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(testTeacher));

        // Act
        Teacher result1 = teacherService.findById(testTeacherId);
        Teacher result2 = teacherService.findById(testTeacherId);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        assertEquals(result1.getFirstName(), result2.getFirstName());

        // Verify repository was called twice
        verify(teacherRepository, times(2)).findById(testTeacherId);
        verifyNoMoreInteractions(teacherRepository);
    }

    // ==================== INTEGRATION SCENARIOS TESTS ====================

    @Test
    void findAllThenFindById_ShouldWorkInSequence() {
        // Arrange
        List<Teacher> teachers = Arrays.asList(testTeacher, testTeacher2);
        when(teacherRepository.findAll()).thenReturn(teachers);
        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(testTeacher));

        // Act
        List<Teacher> allTeachers = teacherService.findAll();
        Teacher specificTeacher = teacherService.findById(testTeacherId);

        // Assert
        assertNotNull(allTeachers);
        assertNotNull(specificTeacher);
        assertEquals(2, allTeachers.size());
        assertEquals(testTeacherId, specificTeacher.getId());
        
        // Verify that the teacher from findById exists in the list from findAll
        assertTrue(allTeachers.stream().anyMatch(t -> t.getId().equals(specificTeacher.getId())));

        // Verify both operations were called
        verify(teacherRepository).findAll();
        verify(teacherRepository).findById(testTeacherId);
        verifyNoMoreInteractions(teacherRepository);
    }
}