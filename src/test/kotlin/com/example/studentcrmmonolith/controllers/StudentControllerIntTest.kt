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

class StudentControllerIntTest:BaseIntTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var studentRepository: StudentRepository
    @Autowired
    private lateinit var courseRepository: CourseRepository
    @Autowired
    private lateinit var junctionRepository: JunctionRepository

    @Test
    fun addStudent() {
        val courseId = courseRepository.save(CourseEntity(0, "math")).id
        val requestBuilder = MockMvcRequestBuilders
            .post("http://localhost:8080/student-api/v1/student")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[$courseId]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))

        val students = studentRepository.findAll()
        val junctions = junctionRepository.findAll()
        assertEquals(1, students.size)
        assertEquals(1, junctions.size)
        assertEquals("John", students[0].firstName)
        assertEquals("Doe", students[0].lastName)
        assertEquals("john.doe@example.com", students[0].email)
        assertEquals(courseId, junctions[0].courseId)
        assertEquals(students[0].id, junctions[0].studentId)
    }

    @Test
    fun addStudentWithAlreadyExistingEmail() {
        studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com"))
        val requestBuilder = MockMvcRequestBuilders
            .post("http://localhost:8080/student-api/v1/student")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.errorMessage").value("Student with Email john.doe@example.com already exists"))

        assertEquals(1, studentRepository.findAll().size)
    }

    @Test
    fun addStudentWithInvalidCourseId() {
        val requestBuilder = MockMvcRequestBuilders
            .post("http://localhost:8080/student-api/v1/student")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[1]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Course with id 1 not found"))

        assertEquals(0, studentRepository.findAll().size)
        assertEquals(0, junctionRepository.findAll().size)
    }

    @Test
    fun getStudent() {
        val studentId = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/student-api/v1/student/$studentId")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
    }

    @Test
    fun getStudentWithInvalidStudentId() {
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/student-api/v1/student/1")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Student with id 1 not found"))
    }

    @Test
    fun getAllStudents() {
        studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com"))
        studentRepository.save(StudentEntity(0, "Jane", "Smith", "jane.smith@example.com"))
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/student-api/v1/students")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[0].lastName").value("Doe"))
            .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
            .andExpect(jsonPath("$[1].firstName").value("Jane"))
            .andExpect(jsonPath("$[1].lastName").value("Smith"))
            .andExpect(jsonPath("$[1].email").value("jane.smith@example.com"))
    }

    @Test
    fun getStudentsOfCourse() {
        val studentId1 = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        val studentId2 = studentRepository.save(StudentEntity(0, "Jane", "Smith", "jane.smith@example.com")).id
        val courseId = courseRepository.save(CourseEntity(0, "math")).id
        junctionRepository.save(JunctionEntity(0, studentId1, courseId))
        junctionRepository.save(JunctionEntity(0, studentId2, courseId))
        val requestBuilder = MockMvcRequestBuilders.get("http://localhost:8080/student-api/v1/students/of_course/$courseId")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[0].lastName").value("Doe"))
            .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
            .andExpect(jsonPath("$[1].firstName").value("Jane"))
            .andExpect(jsonPath("$[1].lastName").value("Smith"))
            .andExpect(jsonPath("$[1].email").value("jane.smith@example.com"))
    }

    @Test
    fun editStudent() {
        val courseId1 = courseRepository.save(CourseEntity(0, "math")).id
        val courseId2 = courseRepository.save(CourseEntity(0, "physics")).id
        val studentId = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        junctionRepository.save(JunctionEntity(0, studentId, courseId1))
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/student-api/v1/student")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":$studentId,"firstName":"John","lastName":"Smith","email":"john.doe@example.com","courseIds":[$courseId2]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Smith"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))

        val students = studentRepository.findAll()
        val junctions = junctionRepository.findAll()
        assertEquals(1, students.size)
        assertEquals(1, junctions.size)
        assertEquals("John", students[0].firstName)
        assertEquals("Smith", students[0].lastName)
        assertEquals("john.doe@example.com", students[0].email)
        assertEquals(studentId, junctions[0].studentId)
        assertEquals(courseId2, junctions[0].courseId)
    }

    @Test
    fun editStudentWithInvalidStudentId() {
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/student-api/v1/student")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":1,"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Student with id 1 not found"))
    }

    @Test
    fun editStudentWithAlreadyExistingEmail() {
        val studentId1 = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        val studentId2 = studentRepository.save(StudentEntity(0, "Jane", "Smith", "jane.smith@example.com")).id
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/student-api/v1/student")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":$studentId1,"firstName":"John","lastName":"Doe","email":"jane.smith@example.com","courseIds":[]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.errorMessage").value("Student with Email jane.smith@example.com already exists"))

        assertEquals("john.doe@example.com", studentRepository.findById(studentId1).get().email)
        assertEquals("jane.smith@example.com", studentRepository.findById(studentId2).get().email)
    }

    @Test
    fun editStudentWithInvalidCourseId() {
        val studentId = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        val requestBuilder = MockMvcRequestBuilders
            .put("http://localhost:8080/student-api/v1/student")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"id":$studentId,"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[1]}""")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Course with id 1 not found"))

        assertEquals(0, junctionRepository.findAll().size)
    }

    @Test
    fun deleteStudent() {
        val courseId1 = courseRepository.save(CourseEntity(0, "math")).id
        val courseId2 = courseRepository.save(CourseEntity(0, "physics")).id
        val studentId = studentRepository.save(StudentEntity(0, "John", "Doe", "john.doe@example.com")).id
        junctionRepository.save(JunctionEntity(0, studentId, courseId1))
        junctionRepository.save(JunctionEntity(0, studentId, courseId2))
        val requestBuilder = MockMvcRequestBuilders.delete("http://localhost:8080/student-api/v1/student/$studentId")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNoContent)

        assertEquals(0, junctionRepository.findAll().size)
        assertEquals(0, studentRepository.findAll().size)
    }

    @Test
    fun deleteStudentWithInvalidStudentId() {
        val requestBuilder = MockMvcRequestBuilders.delete("http://localhost:8080/student-api/v1/student/1")

        mockMvc
            .perform(requestBuilder)
            .andDo(MockMvcRestDocumentation.document("{method-name}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorMessage").value("Student with id 1 not found"))
    }
}