package com.example.studentcrmmonolith.controllers

import com.example.studentcrmmonolith.BaseIntTest
import com.example.studentcrmmonolith.entities.CourseEntity
import com.example.studentcrmmonolith.entities.JunctionEntity
import com.example.studentcrmmonolith.entities.StudentEntity
import com.example.studentcrmmonolith.repositories.CourseRepository
import com.example.studentcrmmonolith.repositories.JunctionRepository
import com.example.studentcrmmonolith.repositories.StudentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CourseControllerIntTest: BaseIntTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var studentRepository: StudentRepository
    @Autowired
    private lateinit var courseRepository: CourseRepository
    @Autowired
    private lateinit var junctionRepository: JunctionRepository

    @Test
    fun addCourse() {
        val studentId = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        val requestBuilder = MockMvcRequestBuilders
            .post("http://localhost:8080/course-api/v1/course")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"math","studentIds":[$studentId]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("math"))

        val courses = courseRepository.findAll()
        val junctions = junctionRepository.findAll()
        assertEquals(1, courses.size)
        assertEquals(1, junctions.size)
        assertEquals("math", courses[0].name)
        assertEquals(studentId, junctions[0].studentId)
        assertEquals(courses[0].id, junctions[0].courseId)
    }

    @Test
    fun addCourseWithAlreadyExistingName() {
        courseRepository.save(CourseEntity(0, "math"))
        val requestBuilder = MockMvcRequestBuilders
            .post("http://localhost:8080/course-api/v1/course")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"math","studentIds":[]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.errorMessage").value("Course with name math already exists"))

        assertEquals(1, courseRepository.findAll().size)
    }

    @Test
    fun addCourseWithInvalidStudentId() {
        val requestBuilder = MockMvcRequestBuilders
            .post("http://localhost:8080/course-api/v1/course")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"math","studentIds":[1]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Student with id 1 not found"))

        assertEquals(0, courseRepository.findAll().size)
        assertEquals(0, junctionRepository.findAll().size)
    }

    @Test
    fun getCourse() {
        val courseId = courseRepository.save(CourseEntity(0, "math")).id
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/course-api/v1/course/$courseId")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("math"))
    }

    @Test
    fun getCourseWithInvalidCourseId() {
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/course-api/v1/course/1")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Course with id 1 not found"))
    }

    @Test
    fun getAllCourses() {
        courseRepository.save(CourseEntity(0, "math"))
        courseRepository.save(CourseEntity(0, "physics"))
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/course-api/v1/courses")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("math"))
            .andExpect(jsonPath("$[1].name").value("physics"))
    }

    @Test
    fun getCoursesOfStudent() {
        val courseId1 = courseRepository.save(CourseEntity(0, "math")).id
        val courseId2 = courseRepository.save(CourseEntity(0, "physics")).id
        val studentId = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        junctionRepository.save(JunctionEntity(0, studentId, courseId1))
        junctionRepository.save(JunctionEntity(0, studentId, courseId2))
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/course-api/v1/courses/of_student/$studentId")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("math"))
            .andExpect(jsonPath("$[1].name").value("physics"))
    }

    @Test
    fun editCourse() {
        val studentId1 = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        val studentId2 = studentRepository.save(StudentEntity(0, "Jane", "Smith", "jane.smith@example.com")).id
        val courseId = courseRepository.save(CourseEntity(0, "math")).id
        junctionRepository.save(JunctionEntity(0, studentId1, courseId))
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/course-api/v1/course")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":$courseId,"name":"physics","studentIds":[$studentId2]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.name").value("physics"))

        val courses = courseRepository.findAll()
        val junctions = junctionRepository.findAll()
        assertEquals(1, courses.size)
        assertEquals(1, junctions.size)
        assertEquals("physics", courses[0].name)
        assertEquals(studentId2, junctions[0].studentId)
        assertEquals(courseId, junctions[0].courseId)
    }

    @Test
    fun editCourseWithInvalidCourseId() {
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/course-api/v1/course")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":1,"name":"math","studentIds":[]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Course with id 1 not found"))
    }

    @Test
    fun editCourseWithAlreadyExistingName() {
        val courseId1 = courseRepository.save(CourseEntity(0, "physics")).id
        val courseId2 = courseRepository.save(CourseEntity(0, "math")).id
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/course-api/v1/course")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":$courseId1,"name":"math","studentIds":[]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.errorMessage").value("Course with name math already exists"))

        assertEquals("physics", courseRepository.findById(courseId1).get().name)
        assertEquals("math", courseRepository.findById(courseId2).get().name)
    }

    @Test
    fun editCourseWithInvalidStudentId() {
        val courseId = courseRepository.save(CourseEntity(0, "physics")).id
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/course-api/v1/course")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":$courseId,"name":"physics","studentIds":[1]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Student with id 1 not found"))

        assertEquals(0, junctionRepository.findAll().size)
    }

    @Test
    fun deleteCourse() {
        val studentId1 = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        val studentId2 = studentRepository.save(StudentEntity(0, "Jane", "Smith", "jane.smith@example.com")).id
        val courseId = courseRepository.save(CourseEntity(0, "math")).id
        junctionRepository.save(JunctionEntity(0, studentId1, courseId))
        junctionRepository.save(JunctionEntity(0, studentId2, courseId))
        val requestBuilder = MockMvcRequestBuilders.delete("http://localhost:8080/course-api/v1/course/$courseId")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNoContent)

        assertEquals(0, junctionRepository.findAll().size)
        assertEquals(0, courseRepository.findAll().size)
    }

    @Test
    fun deleteCourseWithInvalidCourseId() {
        val requestBuilder = MockMvcRequestBuilders.delete("http://localhost:8080/course-api/v1/course/1")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Course with id 1 not found"))
    }
}