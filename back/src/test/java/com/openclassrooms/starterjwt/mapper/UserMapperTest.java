package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserMapper
 * Tests the mapping between User entities and UserDto objects
 */
@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;
    private UserDto testUserDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 8, 21, 10, 0, 0);
        
        testUser = User.builder()
                .id(1L)
                .email("test@yoga.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .admin(false)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setEmail("test@yoga.com");
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setPassword("encodedPassword");
        testUserDto.setAdmin(false);
        testUserDto.setCreatedAt(testDateTime);
        testUserDto.setUpdatedAt(testDateTime);
    }

    @Test
    void testMapperIsNotNull() {
        assertNotNull(userMapper);
    }

    // ==================== TO ENTITY TESTS ====================

    @Test
    void toEntity_ShouldReturnUser_WhenValidUserDto() {
        // Act
        User result = userMapper.toEntity(testUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDto.getId(), result.getId());
        assertEquals(testUserDto.getEmail(), result.getEmail());
        assertEquals(testUserDto.getFirstName(), result.getFirstName());
        assertEquals(testUserDto.getLastName(), result.getLastName());
        assertEquals(testUserDto.getPassword(), result.getPassword());
        assertEquals(testUserDto.isAdmin(), result.isAdmin());
        assertEquals(testUserDto.getCreatedAt(), result.getCreatedAt());
        assertEquals(testUserDto.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenUserDtoIsNull() {
        // Act
        User result = userMapper.toEntity((UserDto) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_ShouldHandleUserDtoWithNullFields() {
        // Arrange
        UserDto userDtoWithNulls = new UserDto();
        userDtoWithNulls.setId(2L);
        userDtoWithNulls.setEmail("partial@yoga.com");
        userDtoWithNulls.setFirstName("John");
        userDtoWithNulls.setLastName("Doe");
        userDtoWithNulls.setPassword("password");
        // Other fields are null

        // Act
        User result = userMapper.toEntity(userDtoWithNulls);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("partial@yoga.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("password", result.getPassword());
        assertFalse(result.isAdmin()); // Default value for boolean
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldHandleAdminUser() {
        // Arrange
        testUserDto.setAdmin(true);

        // Act
        User result = userMapper.toEntity(testUserDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAdmin());
    }

    // ==================== TO DTO TESTS ====================

    @Test
    void toDto_ShouldReturnUserDto_WhenValidUser() {
        // Act
        UserDto result = userMapper.toDto(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals(testUser.isAdmin(), result.isAdmin());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
        assertEquals(testUser.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toDto_ShouldReturnNull_WhenUserIsNull() {
        // Act
        UserDto result = userMapper.toDto((User) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDto_ShouldHandleUserWithMinimalRequiredFields() {
        // Arrange
        User userWithMinimal = User.builder()
                .id(3L)
                .email("minimal@yoga.com")
                .firstName("Minimal")
                .lastName("User")
                .password("password")
                .admin(false)
                .build();

        // Act
        UserDto result = userMapper.toDto(userWithMinimal);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("minimal@yoga.com", result.getEmail());
        assertEquals("Minimal", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("password", result.getPassword());
        assertFalse(result.isAdmin());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toDto_ShouldHandleAdminUser() {
        // Arrange
        User adminUser = User.builder()
                .id(1L)
                .email("admin@yoga.com")
                .firstName("Admin")
                .lastName("User")
                .password("adminPassword")
                .admin(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // Act
        UserDto result = userMapper.toDto(adminUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAdmin());
    }

    // ==================== LIST TO ENTITY TESTS ====================

    @Test
    void toEntityList_ShouldReturnUserList_WhenValidUserDtoList() {
        // Arrange
        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setEmail("user2@yoga.com");
        userDto2.setFirstName("Jane");
        userDto2.setLastName("Smith");
        userDto2.setPassword("password2");
        userDto2.setAdmin(true);

        List<UserDto> userDtoList = Arrays.asList(testUserDto, userDto2);

        // Act
        List<User> result = userMapper.toEntity(userDtoList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        User user1 = result.get(0);
        assertEquals(testUserDto.getId(), user1.getId());
        assertEquals(testUserDto.getEmail(), user1.getEmail());
        assertEquals(testUserDto.getFirstName(), user1.getFirstName());
        assertEquals(testUserDto.getLastName(), user1.getLastName());

        User user2 = result.get(1);
        assertEquals(userDto2.getId(), user2.getId());
        assertEquals(userDto2.getEmail(), user2.getEmail());
        assertEquals(userDto2.getFirstName(), user2.getFirstName());
        assertEquals(userDto2.getLastName(), user2.getLastName());
        assertTrue(user2.isAdmin());
    }

    @Test
    void toEntityList_ShouldReturnNull_WhenUserDtoListIsNull() {
        // Act
        List<User> result = userMapper.toEntity((List<UserDto>) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntityList_ShouldReturnEmptyList_WhenUserDtoListIsEmpty() {
        // Arrange
        List<UserDto> emptyList = Arrays.asList();

        // Act
        List<User> result = userMapper.toEntity(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toEntityList_ShouldHandleListWithNullElements() {
        // Arrange
        List<UserDto> listWithNull = Arrays.asList(testUserDto, null);

        // Act
        List<User> result = userMapper.toEntity(listWithNull);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1));
    }

    // ==================== LIST TO DTO TESTS ====================

    @Test
    void toDtoList_ShouldReturnUserDtoList_WhenValidUserList() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .email("user2@yoga.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("password2")
                .admin(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        List<User> userList = Arrays.asList(testUser, user2);

        // Act
        List<UserDto> result = userMapper.toDto(userList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        UserDto userDto1 = result.get(0);
        assertEquals(testUser.getId(), userDto1.getId());
        assertEquals(testUser.getEmail(), userDto1.getEmail());
        assertEquals(testUser.getFirstName(), userDto1.getFirstName());
        assertEquals(testUser.getLastName(), userDto1.getLastName());

        UserDto userDto2 = result.get(1);
        assertEquals(user2.getId(), userDto2.getId());
        assertEquals(user2.getEmail(), userDto2.getEmail());
        assertEquals(user2.getFirstName(), userDto2.getFirstName());
        assertEquals(user2.getLastName(), userDto2.getLastName());
        assertTrue(userDto2.isAdmin());
    }

    @Test
    void toDtoList_ShouldReturnNull_WhenUserListIsNull() {
        // Act
        List<UserDto> result = userMapper.toDto((List<User>) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDtoList_ShouldReturnEmptyList_WhenUserListIsEmpty() {
        // Arrange
        List<User> emptyList = Arrays.asList();

        // Act
        List<UserDto> result = userMapper.toDto(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDtoList_ShouldHandleListWithNullElements() {
        // Arrange
        List<User> listWithNull = Arrays.asList(testUser, null);

        // Act
        List<UserDto> result = userMapper.toDto(listWithNull);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1));
    }

    // ==================== BIDIRECTIONAL MAPPING TESTS ====================

    @Test
    void bidirectionalMapping_ShouldMaintainDataIntegrity_UserToDto() {
        // Act
        UserDto dto = userMapper.toDto(testUser);
        User backToEntity = userMapper.toEntity(dto);

        // Assert
        assertNotNull(backToEntity);
        assertEquals(testUser.getId(), backToEntity.getId());
        assertEquals(testUser.getEmail(), backToEntity.getEmail());
        assertEquals(testUser.getFirstName(), backToEntity.getFirstName());
        assertEquals(testUser.getLastName(), backToEntity.getLastName());
        assertEquals(testUser.getPassword(), backToEntity.getPassword());
        assertEquals(testUser.isAdmin(), backToEntity.isAdmin());
        assertEquals(testUser.getCreatedAt(), backToEntity.getCreatedAt());
        assertEquals(testUser.getUpdatedAt(), backToEntity.getUpdatedAt());
    }

    @Test
    void bidirectionalMapping_ShouldMaintainDataIntegrity_DtoToUser() {
        // Act
        User entity = userMapper.toEntity(testUserDto);
        UserDto backToDto = userMapper.toDto(entity);

        // Assert
        assertNotNull(backToDto);
        assertEquals(testUserDto.getId(), backToDto.getId());
        assertEquals(testUserDto.getEmail(), backToDto.getEmail());
        assertEquals(testUserDto.getFirstName(), backToDto.getFirstName());
        assertEquals(testUserDto.getLastName(), backToDto.getLastName());
        assertEquals(testUserDto.getPassword(), backToDto.getPassword());
        assertEquals(testUserDto.isAdmin(), backToDto.isAdmin());
        assertEquals(testUserDto.getCreatedAt(), backToDto.getCreatedAt());
        assertEquals(testUserDto.getUpdatedAt(), backToDto.getUpdatedAt());
    }

    @Test
    void bidirectionalListMapping_ShouldMaintainDataIntegrity() {
        // Arrange
        List<User> userList = Arrays.asList(testUser);

        // Act
        List<UserDto> dtoList = userMapper.toDto(userList);
        List<User> backToEntityList = userMapper.toEntity(dtoList);

        // Assert
        assertNotNull(backToEntityList);
        assertEquals(1, backToEntityList.size());
        
        User resultUser = backToEntityList.get(0);
        assertEquals(testUser.getId(), resultUser.getId());
        assertEquals(testUser.getEmail(), resultUser.getEmail());
        assertEquals(testUser.getFirstName(), resultUser.getFirstName());
        assertEquals(testUser.getLastName(), resultUser.getLastName());
        assertEquals(testUser.isAdmin(), resultUser.isAdmin());
    }
}