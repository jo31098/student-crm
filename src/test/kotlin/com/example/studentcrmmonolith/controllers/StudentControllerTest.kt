package com.example.studentcrmmonolith.controllers

import com.example.studentcrmmonolith.dtos.student.StudentDto
import com.example.studentcrmmonolith.exceptions.CourseNotFoundException
import com.example.studentcrmmonolith.exceptions.StudentAlreadyExistsException
import com.example.studentcrmmonolith.services.StudentService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(StudentController::class)
class StudentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var studentService: StudentService

    @Nested
    inner class AddStudent {

        private val studentDto1 = StudentDto(0, "John", "Doe", "john.doe@example.com", listOf(1,2))
        private val studentDto2 = StudentDto(1, "John", "Doe", "john.doe@example.com", listOf())

        @Test
        fun `returns added student`() {
            every { studentService.addStudent(studentDto1) } returns studentDto2

            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(studentDto2.id))
                .andExpect(jsonPath("$.firstName").value(studentDto2.firstName))
                .andExpect(jsonPath("$.lastName").value(studentDto2.lastName))
                .andExpect(jsonPath("$.email").value(studentDto2.email))

        }

        @Test
        fun `fails when firstName ist blank` () {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"","lastName":"Doe","email":"john.doe@example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("First Name is required"))
        }

        @Test
        fun `fails when firstName is too long` () {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"SomeWayTooLongFirstName","lastName":"Doe","email":"john.doe@example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("First Name must be less than 20 characters"))
        }

        @Test
        fun `fails when lastName is blank` () {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"John","lastName":"","email":"john.doe@example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("Last Name is required"))
        }

        @Test
        fun `fails when lastName is too long` () {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"John","lastName":"SomeWayTooLongLastName","email":"john.doe@example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("Last Name must be less than 20 characters"))
        }

        /*@Test
        fun `fails when email is blank` () {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"John","lastName":"Doe","email":"","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("Email is required"))
        }*/

        @Test
        fun `fails when email format is invalid` () {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"John","lastName":"Doe","email":"john.doe.example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("Email format is invalid"))
        }

        @Test
        fun `fails when student already exists` () {
            every { studentService.addStudent(studentDto1) } throws StudentAlreadyExistsException(studentDto1.email)

            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.errorMessage").value("Student with Email john.doe@example.com already exists"))
        }

        @Test
        fun `fails when course does not exist` () {
            every { studentService.addStudent(studentDto1) } throws CourseNotFoundException(1)

            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/student-api/v1/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","courseIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errorMessage").value("Course with id 1 not found"))
        }

    }

}