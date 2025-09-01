package com.openclassrooms.starterjwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SessionController
 * Tests the complete REST API with real Spring context, security, and H2 database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser
class SessionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Session testSession;
    private Teacher testTeacher;
    private Teacher testTeacher2;
    private Teacher testTeacher3;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean database
        sessionRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();

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
                .lastName("PilatesExpert")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testTeacher2 = teacherRepository.save(testTeacher2);

        testTeacher3 = Teacher.builder()
                .firstName("Bob")
                .lastName("FitnessCoach")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testTeacher3 = teacherRepository.save(testTeacher3);

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .admin(false)
                .build();
        testUser = userRepository.save(testUser);

        // Create test session
        testSession = Session.builder()
                .name("Yoga Class")
                .description("Morning yoga session")
                .date(new Date())
                .teacher(testTeacher)
                .users(new ArrayList<>(Arrays.asList(testUser)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testSession = sessionRepository.save(testSession);
    }

    // ==================== FIND BY ID INTEGRATION TESTS ====================

    @Test
    void findById_ShouldReturnSessionDto_WhenValidIdAndSessionExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/session/{id}", testSession.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testSession.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Yoga Class"))
                .andExpect(jsonPath("$.description").value("Morning yoga session"))
                .andExpect(jsonPath("$.teacher_id").value(testTeacher.getId().intValue()));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenValidIdButSessionDoesNotExist() throws Exception {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        mockMvc.perform(get("/api/session/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/session/{id}", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== FIND ALL INTEGRATION TESTS ====================

    @Test
    void findAll_ShouldReturnListOfSessions_WhenSessionsExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testSession.getId().intValue()))
                .andExpect(jsonPath("$[0].name").value("Yoga Class"))
                .andExpect(jsonPath("$[0].description").value("Morning yoga session"));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoSessionsExist() throws Exception {
        // Arrange
        sessionRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/api/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== CREATE INTEGRATION TESTS ====================

    @Test
    void create_ShouldReturnSessionDto_WhenValidData() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("New Session");
        sessionDto.setDescription("New session description");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(testTeacher2.getId());

        // Act & Assert
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Session"))
                .andExpect(jsonPath("$.description").value("New session description"))
                .andExpect(jsonPath("$.teacher_id").exists());
    }

    @Test
    void create_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("");
        sessionDto.setDescription("Description");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(testTeacher2.getId());

        // Act & Assert
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturnBadRequest_WhenDateIsNull() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Session");
        sessionDto.setDescription("Description");
        sessionDto.setDate(null);
        sessionDto.setTeacher_id(testTeacher2.getId());

        // Act & Assert
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturnBadRequest_WhenTeacherIdIsNull() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Session");
        sessionDto.setDescription("Description");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(null);

        // Act & Assert
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isBadRequest());
    }

    // ==================== UPDATE INTEGRATION TESTS ====================

    @Test
    void update_ShouldReturnUpdatedSessionDto_WhenValidData() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Updated Session");
        sessionDto.setDescription("Updated description");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(testTeacher2.getId());

        // Act & Assert
        mockMvc.perform(put("/api/session/{id}", testSession.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Session"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Updated Session");
        sessionDto.setDescription("Updated description");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(testTeacher2.getId());

        // Act & Assert
        mockMvc.perform(put("/api/session/{id}", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE INTEGRATION TESTS ====================

    @Test
    void delete_ShouldReturnOk_WhenSessionExists() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}", testSession.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenSessionDoesNotExist() throws Exception {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== PARTICIPATE INTEGRATION TESTS ====================

    @Test
    void participate_ShouldReturnOk_WhenValidIds() throws Exception {
        // Arrange
        User newUser = User.builder()
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .password("password")
                .admin(false)
                .build();
        newUser = userRepository.save(newUser);

        // Act & Assert
        mockMvc.perform(post("/api/session/{id}/participate/{userId}", testSession.getId(), newUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void participate_ShouldReturnBadRequest_WhenSessionIdNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/session/{id}/participate/{userId}", "not-a-number", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void participate_ShouldReturnBadRequest_WhenUserIdNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/session/{id}/participate/{userId}", testSession.getId(), "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== NO LONGER PARTICIPATE INTEGRATION TESTS ====================

    @Test
    void noLongerParticipate_ShouldReturnOk_WhenValidIds() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}/participate/{userId}", testSession.getId(), testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void noLongerParticipate_ShouldReturnBadRequest_WhenSessionIdNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}/participate/{userId}", "not-a-number", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noLongerParticipate_ShouldReturnBadRequest_WhenUserIdNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}/participate/{userId}", testSession.getId(), "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== HTTP METHODS TESTS ====================

    @Test
    void findById_ShouldNotAcceptPOST() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/session/{id}", testSession.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void findAll_ShouldNotAcceptPOST_WithoutBody() throws Exception {
        // Act & Assert - This would actually work for create, but testing wrong usage
        mockMvc.perform(put("/api/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== DATA PERSISTENCE TESTS ====================

    @Test
    void create_ShouldPersistSessionInDatabase() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Persisted Session");
        sessionDto.setDescription("This should be persisted");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(testTeacher2.getId());

        // Act
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isOk());

        // Assert
        long sessionCount = sessionRepository.count();
        assert sessionCount == 2; // Original + new one
    }

    @Test
    void delete_ShouldRemoveSessionFromDatabase() throws Exception {
        // Act
        mockMvc.perform(delete("/api/session/{id}", testSession.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assert
        long sessionCount = sessionRepository.count();
        assert sessionCount == 0;
    }

    // ==================== MALFORMED JSON TESTS ====================

    @Test
    void create_ShouldReturnBadRequest_WhenMalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldReturnBadRequest_WhenMalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/session/{id}", testSession.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed json"))
                .andExpect(status().isBadRequest());
    }

    // ==================== SPECIAL CHARACTERS TESTS ====================

    @Test
    void create_ShouldHandleSpecialCharactersInName() throws Exception {
        // Arrange
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga & Méditation");
        sessionDto.setDescription("Session with special chars: àáâãäå");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(testTeacher3.getId());

        // Act & Assert
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga & Méditation"))
                .andExpect(jsonPath("$.description").value("Session with special chars: àáâãäå"));
    }
}