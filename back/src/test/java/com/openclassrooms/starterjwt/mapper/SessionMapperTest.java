package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SessionMapper
 * Tests the mapping between Session entities and SessionDto objects
 * Uses real database and services to test complex mapping logic
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SessionMapperTest {

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;


    private Teacher testTeacher;
    private User testUser1;
    private User testUser2;
    private Session testSession;
    private SessionDto testSessionDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 8, 21, 10, 0, 0);
        
        // Create and save a teacher
        testTeacher = Teacher.builder()
                .firstName("John")
                .lastName("YogaMaster")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        testTeacher = teacherRepository.save(testTeacher);

        // Create and save users
        testUser1 = User.builder()
                .email("user1@yoga.com")
                .firstName("User")
                .lastName("One")
                .password("password1")
                .admin(false)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        testUser1 = userRepository.save(testUser1);

        testUser2 = User.builder()
                .email("user2@yoga.com")
                .firstName("User")
                .lastName("Two")
                .password("password2")
                .admin(false)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        testUser2 = userRepository.save(testUser2);

        // Create test session
        testSession = Session.builder()
                .name("Yoga Session")
                .description("A relaxing yoga session")
                .date(new Date())
                .teacher(testTeacher)
                .users(Arrays.asList(testUser1, testUser2))
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // Create test session DTO
        testSessionDto = new SessionDto();
        testSessionDto.setName("Yoga Session DTO");
        testSessionDto.setDescription("A yoga session from DTO");
        testSessionDto.setDate(new Date());
        testSessionDto.setTeacher_id(testTeacher.getId());
        testSessionDto.setUsers(Arrays.asList(testUser1.getId(), testUser2.getId()));
        testSessionDto.setCreatedAt(testDateTime);
        testSessionDto.setUpdatedAt(testDateTime);
    }

    @Test
    void testMapperIsNotNull() {
        assertNotNull(sessionMapper);
    }

    // ==================== TO ENTITY TESTS ====================

    @Test
    void toEntity_ShouldReturnSession_WhenValidSessionDto() {
        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertEquals(testSessionDto.getName(), result.getName());
        assertEquals(testSessionDto.getDescription(), result.getDescription());
        assertEquals(testSessionDto.getDate(), result.getDate());
        assertEquals(testSessionDto.getCreatedAt(), result.getCreatedAt());
        assertEquals(testSessionDto.getUpdatedAt(), result.getUpdatedAt());
        
        // Check teacher mapping
        assertNotNull(result.getTeacher());
        assertEquals(testTeacher.getId(), result.getTeacher().getId());
        assertEquals(testTeacher.getFirstName(), result.getTeacher().getFirstName());
        assertEquals(testTeacher.getLastName(), result.getTeacher().getLastName());
        
        // Check users mapping
        assertNotNull(result.getUsers());
        assertEquals(2, result.getUsers().size());
        assertTrue(result.getUsers().stream().anyMatch(user -> user.getId().equals(testUser1.getId())));
        assertTrue(result.getUsers().stream().anyMatch(user -> user.getId().equals(testUser2.getId())));
    }

    @Test
    void toEntity_ShouldReturnNull_WhenSessionDtoIsNull() {
        // Act
        Session result = sessionMapper.toEntity((SessionDto) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_ShouldHandleNullTeacherId() {
        // Arrange
        testSessionDto.setTeacher_id(null);

        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertNull(result.getTeacher());
    }

    @Test
    void toEntity_ShouldHandleNullUsersList() {
        // Arrange
        testSessionDto.setUsers(null);

        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUsers());
        assertTrue(result.getUsers().isEmpty());
    }

    @Test
    void toEntity_ShouldHandleEmptyUsersList() {
        // Arrange
        testSessionDto.setUsers(Arrays.asList());

        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUsers());
        assertTrue(result.getUsers().isEmpty());
    }

    @Test
    void toEntity_ShouldHandleNonExistentTeacherId() {
        // Arrange
        testSessionDto.setTeacher_id(999L);

        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertNull(result.getTeacher());
    }

    @Test
    void toEntity_ShouldFilterOutNonExistentUsers() {
        // Arrange
        testSessionDto.setUsers(Arrays.asList(testUser1.getId(), 999L, testUser2.getId()));

        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUsers());
        // Should contain 3 elements: 2 valid users + 1 null for non-existent user
        assertEquals(3, result.getUsers().size());
        
        // Filter out nulls to count actual users
        long actualUserCount = result.getUsers().stream().filter(user -> user != null).count();
        assertEquals(2, actualUserCount);
    }

    // ==================== TO DTO TESTS ====================

    @Test
    void toDto_ShouldReturnSessionDto_WhenValidSession() {
        // Act
        SessionDto result = sessionMapper.toDto(testSession);

        // Assert
        assertNotNull(result);
        assertEquals(testSession.getName(), result.getName());
        assertEquals(testSession.getDescription(), result.getDescription());
        assertEquals(testSession.getDate(), result.getDate());
        assertEquals(testSession.getCreatedAt(), result.getCreatedAt());
        assertEquals(testSession.getUpdatedAt(), result.getUpdatedAt());
        
        // Check teacher mapping
        assertNotNull(result.getTeacher_id());
        assertEquals(testTeacher.getId(), result.getTeacher_id());
        
        // Check users mapping
        assertNotNull(result.getUsers());
        assertEquals(2, result.getUsers().size());
        assertTrue(result.getUsers().contains(testUser1.getId()));
        assertTrue(result.getUsers().contains(testUser2.getId()));
    }

    @Test
    void toDto_ShouldReturnNull_WhenSessionIsNull() {
        // Act
        SessionDto result = sessionMapper.toDto((Session) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDto_ShouldHandleNullTeacher() {
        // Arrange
        testSession.setTeacher(null);

        // Act
        SessionDto result = sessionMapper.toDto(testSession);

        // Assert
        assertNotNull(result);
        assertNull(result.getTeacher_id());
    }

    @Test
    void toDto_ShouldHandleNullUsersList() {
        // Arrange
        testSession.setUsers(null);

        // Act
        SessionDto result = sessionMapper.toDto(testSession);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUsers());
        assertTrue(result.getUsers().isEmpty());
    }

    @Test
    void toDto_ShouldHandleEmptyUsersList() {
        // Arrange
        testSession.setUsers(Arrays.asList());

        // Act
        SessionDto result = sessionMapper.toDto(testSession);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUsers());
        assertTrue(result.getUsers().isEmpty());
    }

    @Test
    void toDto_ShouldHandleTeacherWithNullId() {
        // Arrange
        Teacher teacherWithNullId = Teacher.builder()
                .firstName("Test")
                .lastName("Teacher")
                .build();
        // Don't save to keep ID null
        testSession.setTeacher(teacherWithNullId);

        // Act
        SessionDto result = sessionMapper.toDto(testSession);

        // Assert
        assertNotNull(result);
        assertNull(result.getTeacher_id());
    }

    // ==================== LIST TESTS ====================

    @Test
    void toEntityList_ShouldReturnSessionList_WhenValidSessionDtoList() {
        // Arrange
        SessionDto sessionDto2 = new SessionDto();
        sessionDto2.setName("Second Session");
        sessionDto2.setDescription("Another session");
        sessionDto2.setDate(new Date());
        sessionDto2.setTeacher_id(testTeacher.getId());
        sessionDto2.setUsers(Arrays.asList(testUser1.getId()));

        List<SessionDto> sessionDtoList = Arrays.asList(testSessionDto, sessionDto2);

        // Act
        List<Session> result = sessionMapper.toEntity(sessionDtoList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Session session1 = result.get(0);
        assertEquals(testSessionDto.getName(), session1.getName());
        assertEquals(testSessionDto.getDescription(), session1.getDescription());
        
        Session session2 = result.get(1);
        assertEquals(sessionDto2.getName(), session2.getName());
        assertEquals(sessionDto2.getDescription(), session2.getDescription());
    }

    @Test
    void toEntityList_ShouldReturnNull_WhenSessionDtoListIsNull() {
        // Act
        List<Session> result = sessionMapper.toEntity((List<SessionDto>) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntityList_ShouldReturnEmptyList_WhenSessionDtoListIsEmpty() {
        // Arrange
        List<SessionDto> emptyList = Arrays.asList();

        // Act
        List<Session> result = sessionMapper.toEntity(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDtoList_ShouldReturnSessionDtoList_WhenValidSessionList() {
        // Arrange
        Session session2 = Session.builder()
                .name("Second Session")
                .description("Another session")
                .date(new Date())
                .teacher(testTeacher)
                .users(Arrays.asList(testUser1))
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        List<Session> sessionList = Arrays.asList(testSession, session2);

        // Act
        List<SessionDto> result = sessionMapper.toDto(sessionList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        SessionDto sessionDto1 = result.get(0);
        assertEquals(testSession.getName(), sessionDto1.getName());
        assertEquals(testSession.getDescription(), sessionDto1.getDescription());
        
        SessionDto sessionDto2 = result.get(1);
        assertEquals(session2.getName(), sessionDto2.getName());
        assertEquals(session2.getDescription(), sessionDto2.getDescription());
    }

    @Test
    void toDtoList_ShouldReturnNull_WhenSessionListIsNull() {
        // Act
        List<SessionDto> result = sessionMapper.toDto((List<Session>) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDtoList_ShouldReturnEmptyList_WhenSessionListIsEmpty() {
        // Arrange
        List<Session> emptyList = Arrays.asList();

        // Act
        List<SessionDto> result = sessionMapper.toDto(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== BIDIRECTIONAL MAPPING TESTS ====================

    @Test
    void bidirectionalMapping_ShouldMaintainDataIntegrity_SessionToDto() {
        // Act
        SessionDto dto = sessionMapper.toDto(testSession);
        Session backToEntity = sessionMapper.toEntity(dto);

        // Assert
        assertNotNull(backToEntity);
        assertEquals(testSession.getName(), backToEntity.getName());
        assertEquals(testSession.getDescription(), backToEntity.getDescription());
        assertEquals(testSession.getDate(), backToEntity.getDate());
        
        // Teacher should be the same
        assertNotNull(backToEntity.getTeacher());
        assertEquals(testSession.getTeacher().getId(), backToEntity.getTeacher().getId());
        
        // Users should be the same (though order might differ)
        assertEquals(testSession.getUsers().size(), backToEntity.getUsers().size());
    }

    @Test
    void bidirectionalMapping_ShouldMaintainDataIntegrity_DtoToSession() {
        // Act
        Session entity = sessionMapper.toEntity(testSessionDto);
        SessionDto backToDto = sessionMapper.toDto(entity);

        // Assert
        assertNotNull(backToDto);
        assertEquals(testSessionDto.getName(), backToDto.getName());
        assertEquals(testSessionDto.getDescription(), backToDto.getDescription());
        assertEquals(testSessionDto.getDate(), backToDto.getDate());
        assertEquals(testSessionDto.getTeacher_id(), backToDto.getTeacher_id());
        
        // Users list should contain the same IDs
        assertEquals(testSessionDto.getUsers().size(), backToDto.getUsers().size());
        assertTrue(backToDto.getUsers().containsAll(testSessionDto.getUsers()));
    }

    // ==================== COMPLEX SCENARIOS TESTS ====================

    @Test
    void mapping_ShouldHandleSessionWithSingleUser() {
        // Arrange
        testSessionDto.setUsers(Arrays.asList(testUser1.getId()));

        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUsers());
        assertEquals(1, result.getUsers().size());
        assertEquals(testUser1.getId(), result.getUsers().get(0).getId());
    }

    @Test
    void mapping_ShouldHandleSessionWithManyUsers() {
        // Arrange - Create additional users
        User user3 = User.builder()
                .email("user3@yoga.com")
                .firstName("User")
                .lastName("Three")
                .password("password3")
                .admin(false)
                .build();
        user3 = userRepository.save(user3);

        User user4 = User.builder()
                .email("user4@yoga.com")
                .firstName("User")
                .lastName("Four")
                .password("password4")
                .admin(false)
                .build();
        user4 = userRepository.save(user4);

        testSessionDto.setUsers(Arrays.asList(testUser1.getId(), testUser2.getId(), user3.getId(), user4.getId()));

        // Act
        Session result = sessionMapper.toEntity(testSessionDto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUsers());
        assertEquals(4, result.getUsers().size());
        
        List<Long> resultUserIds = result.getUsers().stream()
                .filter(user -> user != null)
                .map(User::getId)
                .toList();
        
        assertTrue(resultUserIds.contains(testUser1.getId()));
        assertTrue(resultUserIds.contains(testUser2.getId()));
        assertTrue(resultUserIds.contains(user3.getId()));
        assertTrue(resultUserIds.contains(user4.getId()));
    }
}