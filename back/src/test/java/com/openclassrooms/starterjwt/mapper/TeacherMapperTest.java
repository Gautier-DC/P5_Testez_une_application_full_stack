package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TeacherMapper
 * Tests the mapping between Teacher entities and TeacherDto objects
 */
@SpringBootTest
class TeacherMapperTest {

    @Autowired
    private TeacherMapper teacherMapper;

    private Teacher testTeacher;
    private TeacherDto testTeacherDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 8, 21, 10, 0, 0);
        
        testTeacher = Teacher.builder()
                .id(1L)
                .firstName("John")
                .lastName("YogaMaster")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        testTeacherDto = new TeacherDto();
        testTeacherDto.setId(1L);
        testTeacherDto.setFirstName("John");
        testTeacherDto.setLastName("YogaMaster");
        testTeacherDto.setCreatedAt(testDateTime);
        testTeacherDto.setUpdatedAt(testDateTime);
    }

    @Test
    void testMapperIsNotNull() {
        assertNotNull(teacherMapper);
    }

    // ==================== TO ENTITY TESTS ====================

    @Test
    void toEntity_ShouldReturnTeacher_WhenValidTeacherDto() {
        // Act
        Teacher result = teacherMapper.toEntity(testTeacherDto);

        // Assert
        assertNotNull(result);
        assertEquals(testTeacherDto.getId(), result.getId());
        assertEquals(testTeacherDto.getFirstName(), result.getFirstName());
        assertEquals(testTeacherDto.getLastName(), result.getLastName());
        assertEquals(testTeacherDto.getCreatedAt(), result.getCreatedAt());
        assertEquals(testTeacherDto.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenTeacherDtoIsNull() {
        // Act
        Teacher result = teacherMapper.toEntity((TeacherDto) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_ShouldHandleTeacherDtoWithNullFields() {
        // Arrange
        TeacherDto teacherDtoWithNulls = new TeacherDto();
        teacherDtoWithNulls.setId(2L);
        teacherDtoWithNulls.setFirstName("Jane");
        // Other fields are null

        // Act
        Teacher result = teacherMapper.toEntity(teacherDtoWithNulls);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Jane", result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldHandleTeacherDtoWithMinimalData() {
        // Arrange
        TeacherDto minimalDto = new TeacherDto();
        minimalDto.setFirstName("Minimal");
        minimalDto.setLastName("Teacher");

        // Act
        Teacher result = teacherMapper.toEntity(minimalDto);

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Minimal", result.getFirstName());
        assertEquals("Teacher", result.getLastName());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    // ==================== TO DTO TESTS ====================

    @Test
    void toDto_ShouldReturnTeacherDto_WhenValidTeacher() {
        // Act
        TeacherDto result = teacherMapper.toDto(testTeacher);

        // Assert
        assertNotNull(result);
        assertEquals(testTeacher.getId(), result.getId());
        assertEquals(testTeacher.getFirstName(), result.getFirstName());
        assertEquals(testTeacher.getLastName(), result.getLastName());
        assertEquals(testTeacher.getCreatedAt(), result.getCreatedAt());
        assertEquals(testTeacher.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toDto_ShouldReturnNull_WhenTeacherIsNull() {
        // Act
        TeacherDto result = teacherMapper.toDto((Teacher) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDto_ShouldHandleTeacherWithNullFields() {
        // Arrange
        Teacher teacherWithNulls = Teacher.builder()
                .id(3L)
                .firstName("Partial")
                .build();

        // Act
        TeacherDto result = teacherMapper.toDto(teacherWithNulls);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Partial", result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toDto_ShouldHandleTeacherWithLongNames() {
        // Arrange
        Teacher teacherWithLongNames = Teacher.builder()
                .id(4L)
                .firstName("VeryLongFirstName")
                .lastName("VeryLongLastName")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // Act
        TeacherDto result = teacherMapper.toDto(teacherWithLongNames);

        // Assert
        assertNotNull(result);
        assertEquals("VeryLongFirstName", result.getFirstName());
        assertEquals("VeryLongLastName", result.getLastName());
    }

    // ==================== LIST TO ENTITY TESTS ====================

    @Test
    void toEntityList_ShouldReturnTeacherList_WhenValidTeacherDtoList() {
        // Arrange
        TeacherDto teacherDto2 = new TeacherDto();
        teacherDto2.setId(2L);
        teacherDto2.setFirstName("Jane");
        teacherDto2.setLastName("YogaExpert");
        teacherDto2.setCreatedAt(testDateTime);
        teacherDto2.setUpdatedAt(testDateTime);

        List<TeacherDto> teacherDtoList = Arrays.asList(testTeacherDto, teacherDto2);

        // Act
        List<Teacher> result = teacherMapper.toEntity(teacherDtoList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        Teacher teacher1 = result.get(0);
        assertEquals(testTeacherDto.getId(), teacher1.getId());
        assertEquals(testTeacherDto.getFirstName(), teacher1.getFirstName());
        assertEquals(testTeacherDto.getLastName(), teacher1.getLastName());

        Teacher teacher2 = result.get(1);
        assertEquals(teacherDto2.getId(), teacher2.getId());
        assertEquals(teacherDto2.getFirstName(), teacher2.getFirstName());
        assertEquals(teacherDto2.getLastName(), teacher2.getLastName());
    }

    @Test
    void toEntityList_ShouldReturnNull_WhenTeacherDtoListIsNull() {
        // Act
        List<Teacher> result = teacherMapper.toEntity((List<TeacherDto>) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntityList_ShouldReturnEmptyList_WhenTeacherDtoListIsEmpty() {
        // Arrange
        List<TeacherDto> emptyList = Arrays.asList();

        // Act
        List<Teacher> result = teacherMapper.toEntity(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toEntityList_ShouldHandleListWithNullElements() {
        // Arrange
        List<TeacherDto> listWithNull = Arrays.asList(testTeacherDto, null);

        // Act
        List<Teacher> result = teacherMapper.toEntity(listWithNull);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1));
    }

    @Test
    void toEntityList_ShouldHandleLargeList() {
        // Arrange
        List<TeacherDto> largeList = Arrays.asList(
                testTeacherDto, testTeacherDto, testTeacherDto, testTeacherDto, testTeacherDto
        );

        // Act
        List<Teacher> result = teacherMapper.toEntity(largeList);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        result.forEach(teacher -> {
            assertNotNull(teacher);
            assertEquals(testTeacherDto.getFirstName(), teacher.getFirstName());
            assertEquals(testTeacherDto.getLastName(), teacher.getLastName());
        });
    }

    // ==================== LIST TO DTO TESTS ====================

    @Test
    void toDtoList_ShouldReturnTeacherDtoList_WhenValidTeacherList() {
        // Arrange
        Teacher teacher2 = Teacher.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("YogaExpert")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        List<Teacher> teacherList = Arrays.asList(testTeacher, teacher2);

        // Act
        List<TeacherDto> result = teacherMapper.toDto(teacherList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        TeacherDto teacherDto1 = result.get(0);
        assertEquals(testTeacher.getId(), teacherDto1.getId());
        assertEquals(testTeacher.getFirstName(), teacherDto1.getFirstName());
        assertEquals(testTeacher.getLastName(), teacherDto1.getLastName());

        TeacherDto teacherDto2 = result.get(1);
        assertEquals(teacher2.getId(), teacherDto2.getId());
        assertEquals(teacher2.getFirstName(), teacherDto2.getFirstName());
        assertEquals(teacher2.getLastName(), teacherDto2.getLastName());
    }

    @Test
    void toDtoList_ShouldReturnNull_WhenTeacherListIsNull() {
        // Act
        List<TeacherDto> result = teacherMapper.toDto((List<Teacher>) null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDtoList_ShouldReturnEmptyList_WhenTeacherListIsEmpty() {
        // Arrange
        List<Teacher> emptyList = Arrays.asList();

        // Act
        List<TeacherDto> result = teacherMapper.toDto(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDtoList_ShouldHandleListWithNullElements() {
        // Arrange
        List<Teacher> listWithNull = Arrays.asList(testTeacher, null);

        // Act
        List<TeacherDto> result = teacherMapper.toDto(listWithNull);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1));
    }

    // ==================== BIDIRECTIONAL MAPPING TESTS ====================

    @Test
    void bidirectionalMapping_ShouldMaintainDataIntegrity_TeacherToDto() {
        // Act
        TeacherDto dto = teacherMapper.toDto(testTeacher);
        Teacher backToEntity = teacherMapper.toEntity(dto);

        // Assert
        assertNotNull(backToEntity);
        assertEquals(testTeacher.getId(), backToEntity.getId());
        assertEquals(testTeacher.getFirstName(), backToEntity.getFirstName());
        assertEquals(testTeacher.getLastName(), backToEntity.getLastName());
        assertEquals(testTeacher.getCreatedAt(), backToEntity.getCreatedAt());
        assertEquals(testTeacher.getUpdatedAt(), backToEntity.getUpdatedAt());
    }

    @Test
    void bidirectionalMapping_ShouldMaintainDataIntegrity_DtoToTeacher() {
        // Act
        Teacher entity = teacherMapper.toEntity(testTeacherDto);
        TeacherDto backToDto = teacherMapper.toDto(entity);

        // Assert
        assertNotNull(backToDto);
        assertEquals(testTeacherDto.getId(), backToDto.getId());
        assertEquals(testTeacherDto.getFirstName(), backToDto.getFirstName());
        assertEquals(testTeacherDto.getLastName(), backToDto.getLastName());
        assertEquals(testTeacherDto.getCreatedAt(), backToDto.getCreatedAt());
        assertEquals(testTeacherDto.getUpdatedAt(), backToDto.getUpdatedAt());
    }

    @Test
    void bidirectionalListMapping_ShouldMaintainDataIntegrity() {
        // Arrange
        List<Teacher> teacherList = Arrays.asList(testTeacher);

        // Act
        List<TeacherDto> dtoList = teacherMapper.toDto(teacherList);
        List<Teacher> backToEntityList = teacherMapper.toEntity(dtoList);

        // Assert
        assertNotNull(backToEntityList);
        assertEquals(1, backToEntityList.size());
        
        Teacher resultTeacher = backToEntityList.get(0);
        assertEquals(testTeacher.getId(), resultTeacher.getId());
        assertEquals(testTeacher.getFirstName(), resultTeacher.getFirstName());
        assertEquals(testTeacher.getLastName(), resultTeacher.getLastName());
        assertEquals(testTeacher.getCreatedAt(), resultTeacher.getCreatedAt());
        assertEquals(testTeacher.getUpdatedAt(), resultTeacher.getUpdatedAt());
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    void mapping_ShouldHandleSpecialCharacters() {
        // Arrange
        Teacher teacherWithSpecialChars = Teacher.builder()
                .id(5L)
                .firstName("José")
                .lastName("María-José")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // Act
        TeacherDto dto = teacherMapper.toDto(teacherWithSpecialChars);
        Teacher backToEntity = teacherMapper.toEntity(dto);

        // Assert
        assertEquals("José", dto.getFirstName());
        assertEquals("María-José", dto.getLastName());
        assertEquals("José", backToEntity.getFirstName());
        assertEquals("María-José", backToEntity.getLastName());
    }

    @Test
    void mapping_ShouldHandleSingleCharacterNames() {
        // Arrange
        Teacher teacherWithSingleChars = Teacher.builder()
                .id(6L)
                .firstName("A")
                .lastName("B")
                .build();

        // Act
        TeacherDto dto = teacherMapper.toDto(teacherWithSingleChars);

        // Assert
        assertEquals("A", dto.getFirstName());
        assertEquals("B", dto.getLastName());
    }
}