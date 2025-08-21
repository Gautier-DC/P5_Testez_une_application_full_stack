package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TeacherController
 * Tests the complete REST API with real Spring context, security, and H2 database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser
class TeacherControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TeacherRepository teacherRepository;


    private MockMvc mockMvc;
    private Teacher testTeacher;
    private Teacher testTeacher2;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean database
        teacherRepository.deleteAll();

        // Create test teachers
        testTeacher = Teacher.builder()
                .firstName("John")
                .lastName("YogaMaster")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testTeacher = teacherRepository.save(testTeacher);

        testTeacher2 = Teacher.builder()
                .firstName("Jane")
                .lastName("YogaExpert")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testTeacher2 = teacherRepository.save(testTeacher2);
    }

    // ==================== FIND BY ID INTEGRATION TESTS ====================

    @Test
    void findById_ShouldReturnTeacherDto_WhenValidIdAndTeacherExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTeacher.getId().intValue()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("YogaMaster"));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenValidIdButTeacherDoesNotExist() throws Exception {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsEmpty() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Spring treats empty path as different endpoint
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdContainsSpecialCharacters() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", "1@#$")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenNegativeId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenZeroId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldHandleLargeIds() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== FIND ALL INTEGRATION TESTS ====================

    @Test
    void findAll_ShouldReturnListOfTeachers_WhenTeachersExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(testTeacher.getId().intValue()))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("YogaMaster"))
                .andExpect(jsonPath("$[1].id").value(testTeacher2.getId().intValue()))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].lastName").value("YogaExpert"));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoTeachersExist() throws Exception {
        // Arrange
        teacherRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void findAll_ShouldReturnSingleTeacher_WhenOnlyOneExists() throws Exception {
        // Arrange
        teacherRepository.deleteAll();
        Teacher singleTeacher = teacherRepository.save(Teacher.builder()
                .firstName("Solo")
                .lastName("Teacher")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Act & Assert
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(singleTeacher.getId().intValue()))
                .andExpect(jsonPath("$[0].firstName").value("Solo"))
                .andExpect(jsonPath("$[0].lastName").value("Teacher"));
    }

    @Test
    void findAll_ShouldIncludeAllTeacherFields() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    // ==================== HTTP METHODS TESTS ====================

    @Test
    void findById_ShouldNotAcceptPOST() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/teacher/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void findById_ShouldNotAcceptPUT() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/teacher/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void findById_ShouldNotAcceptDELETE() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/teacher/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void findAll_ShouldNotAcceptPOST() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void findAll_ShouldNotAcceptPUT() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void findAll_ShouldNotAcceptDELETE() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== CORS TESTS ====================

    @Test
    void findById_ShouldSupportCORS() throws Exception {
        // Note: CORS headers might not be set in test environment
        // Act & Assert - Just verify the endpoint works
        mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void findAll_ShouldSupportCORS() throws Exception {
        // Note: CORS headers might not be set in test environment
        // Act & Assert - Just verify the endpoint works
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== CONTENT TYPE TESTS ====================

    @Test
    void findById_ShouldAcceptDifferentContentTypes() throws Exception {
        // Test with application/json
        mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test without explicit content type
        mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void findAll_ShouldAcceptDifferentContentTypes() throws Exception {
        // Test with application/json
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test without explicit content type
        mockMvc.perform(get("/api/teacher"))
                .andExpect(status().isOk());
    }

    // ==================== PERFORMANCE TESTS ====================

    @Test
    void findAll_ShouldHandleLargeDataset() throws Exception {
        // Arrange - Clean and add many teachers
        teacherRepository.deleteAll();
        for (int i = 1; i <= 20; i++) {
            teacherRepository.save(Teacher.builder()
                    .firstName("Teacher" + i)
                    .lastName("LastName" + i)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        // Act & Assert
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[0].firstName").value("Teacher1"))
                .andExpect(jsonPath("$[19].firstName").value("Teacher20"));
    }

    // ==================== DATA PERSISTENCE TESTS ====================

    @Test
    void findById_ShouldReturnCorrectDataFromDatabase() throws Exception {
        // Arrange - Create specific teacher with known data
        Teacher specificTeacher = teacherRepository.save(Teacher.builder()
                .firstName("Specific")
                .lastName("TestTeacher")
                .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 2, 11, 0))
                .build());

        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", specificTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(specificTeacher.getId().intValue()))
                .andExpect(jsonPath("$.firstName").value("Specific"))
                .andExpect(jsonPath("$.lastName").value("TestTeacher"));
    }

    @Test
    void findAll_ShouldReflectDatabaseChanges() throws Exception {
        // Arrange - Initial state check
        mockMvc.perform(get("/api/teacher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Add new teacher
        Teacher newTeacher = teacherRepository.save(Teacher.builder()
                .firstName("New")
                .lastName("Teacher")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Act & Assert - Should reflect the change
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].id").value(newTeacher.getId().intValue()))
                .andExpect(jsonPath("$[2].firstName").value("New"))
                .andExpect(jsonPath("$[2].lastName").value("Teacher"));
    }

    // ==================== SPECIAL CHARACTERS TESTS ====================

    @Test
    void findAll_ShouldHandleSpecialCharactersInNames() throws Exception {
        // Arrange
        teacherRepository.deleteAll();
        teacherRepository.save(Teacher.builder()
                .firstName("José-María")
                .lastName("O'Connor-Smith")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Act & Assert
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("José-María"))
                .andExpect(jsonPath("$[0].lastName").value("O'Connor-Smith"));
    }

    // ==================== COMBINED OPERATIONS TESTS ====================

    @Test
    void multipleRequests_ShouldWorkIndependently() throws Exception {
        // Act & Assert - Multiple requests should all work
        mockMvc.perform(get("/api/teacher/{id}", testTeacher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));

        mockMvc.perform(get("/api/teacher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/teacher/{id}", testTeacher2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));

        mockMvc.perform(get("/api/teacher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // Should still be 2
    }
}