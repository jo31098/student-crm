package com.example.studentcrmmonolith.services

import com.example.studentcrmmonolith.dtos.JunctionDto
import com.example.studentcrmmonolith.entities.JunctionEntity
import com.example.studentcrmmonolith.exceptions.CourseNotFoundException
import com.example.studentcrmmonolith.exceptions.StudentNotFoundException
import com.example.studentcrmmonolith.repositories.CourseRepository
import com.example.studentcrmmonolith.repositories.JunctionRepository
import com.example.studentcrmmonolith.repositories.StudentRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JunctionServiceTest {

    private val junctionRepository = mockk<JunctionRepository>()
    private val studentRepository = mockk<StudentRepository>()
    private val courseRepository = mockk<CourseRepository>()

    private val cut = JunctionService(junctionRepository, studentRepository, courseRepository)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    inner class AddJunction {

        private val junctionDto = JunctionDto(1,1)
        private val junctionEntity = JunctionEntity(1,1,1)

        @Test
        fun `persists junction`() {
            val slot = slot<JunctionEntity>()
            every { studentRepository.existsById(1) } returns true
            every { courseRepository.existsById(1) } returns true
            every { junctionRepository.save(capture(slot)) } returns junctionEntity

            cut.addJunction(junctionDto)

            assertEquals(0, slot.captured.id)
            assertEquals(junctionDto.studentId, slot.captured.studentId)
            assertEquals(junctionDto.courseId, slot.captured.courseId)
        }

        @Test
        fun `fails when student does not exist`() {
            every { studentRepository.existsById(1) } returns false

            val exception = assertThrows<StudentNotFoundException> { cut.addJunction(junctionDto) }

            assertEquals("Student with id 1 not found", exception.message)
        }

        @Test
        fun `fails when course does not exist`() {
            every { studentRepository.existsById(1) } returns true
            every { courseRepository.existsById(1) } returns false

            val exception = assertThrows<CourseNotFoundException> { cut.addJunction(junctionDto) }

            assertEquals("Course with id 1 not found", exception.message)
        }

    }

}