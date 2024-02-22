package com.example.studentcrmmonolith.controllers

import com.example.studentcrmmonolith.dtos.course.CourseDto
import com.example.studentcrmmonolith.exceptions.CourseAlreadyExistsException
import com.example.studentcrmmonolith.exceptions.StudentNotFoundException
import com.example.studentcrmmonolith.services.CourseService
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

@WebMvcTest(CourseController::class)
class CourseControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var courseService: CourseService

    @Nested
    inner class AddCourse {

        private val courseDto1 = CourseDto(0, "math", listOf(1,2))
        private val courseDto2 = CourseDto(1, "math", listOf())

        @Test
        fun `returns added course`() {
            every { courseService.addCourse(courseDto1) } returns courseDto2

            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/course-api/v1/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"math","studentIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(courseDto2.id))
                .andExpect(jsonPath("$.name").value(courseDto2.name))
        }

        @Test
        fun `fails when course name is blank`() {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/course-api/v1/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","studentIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("Course Name is required"))
        }

        @Test
        fun `fails when course name is too long`() {
            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/course-api/v1/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"A Course Name That Is Waaaaaaay Too Long","studentIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorMessage").value("Course Name must be less than 35 characters"))
        }

        @Test
        fun `fails when course already exists`() {
            every { courseService.addCourse(courseDto1) } throws CourseAlreadyExistsException(courseDto1.name)

            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/course-api/v1/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"math","studentIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.errorMessage").value("Course with name ${courseDto1.name} already exists"))
        }

        @Test
        fun `fails when student does not exist`() {
            every { courseService.addCourse(courseDto1) } throws StudentNotFoundException(courseDto1.studentIds[1])

            val requestBuilder = MockMvcRequestBuilders
                .post("http://localhost:8080/course-api/v1/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"math","studentIds":[1,2]}""")

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errorMessage").value("Student with id ${courseDto1.studentIds[1]} not found"))
        }

    }

}