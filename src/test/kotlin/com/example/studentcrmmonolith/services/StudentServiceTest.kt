package com.example.studentcrmmonolith.services

import com.example.studentcrmmonolith.dtos.JunctionDto
import com.example.studentcrmmonolith.dtos.student.StudentDto
import com.example.studentcrmmonolith.entities.StudentEntity
import com.example.studentcrmmonolith.exceptions.StudentAlreadyExistsException
import com.example.studentcrmmonolith.exceptions.StudentNotFoundException
import com.example.studentcrmmonolith.repositories.StudentRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class StudentServiceTest {

    private val studentRepository = mockk<StudentRepository>()
    private val junctionService = mockk<JunctionService>()

    private val cut = StudentService(studentRepository, junctionService)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    inner class AddStudent {

        private val studentDto = StudentDto(0, "John", "Doe", "john.doe@example.com", listOf(1,2))
        private val studentEntity = StudentEntity(1, "John", "Doe", "john.doe@example.com")

        @Test
        fun `returns correct StudentDto`() {
            every { studentRepository.existsByEmail("john.doe@example.com") } returns false
            every { studentRepository.save(any()) } returns studentEntity
            every { junctionService.addJunction(any()) } just runs

            val result =  cut.addStudent(studentDto)

            assertEquals(studentEntity.toDto(), result)
        }

        @Test
        fun `persists data`() {
            val slot = slot<StudentEntity>()
            every { studentRepository.existsByEmail("john.doe@example.com") } returns false
            every { studentRepository.save(capture(slot)) } returns studentEntity
            every { junctionService.addJunction(any()) } just runs

            cut.addStudent(studentDto)

            assertEquals(studentDto.id, slot.captured.id)
            assertEquals(studentDto.firstName, slot.captured.firstName)
            assertEquals(studentDto.lastName, slot.captured.lastName)
            assertEquals(studentDto.email, slot.captured.email)
            verify { junctionService.addJunction(JunctionDto(1,1)) }
            verify { junctionService.addJunction(JunctionDto(1,2)) }
        }

        @Test
        fun `fails when email address is already taken`() {
            every { studentRepository.existsByEmail("john.doe@example.com") } returns true

            val exception = assertThrows<StudentAlreadyExistsException> { cut.addStudent(studentDto) }

            assertEquals("Student with Email john.doe@example.com already exists", exception.message)
        }

    }

    @Nested
    inner class GetStudent {

        private val studentEntity = StudentEntity(1, "John", "Doe", "john.doe@example.com")

        @Test
        fun `returns correct StudentDto`() {
            every { studentRepository.findById(1) } returns Optional.of(studentEntity)

            val result = cut.getStudent(1)

            assertEquals(studentEntity.toDto(), result)
        }

        @Test
        fun `fails when student does not exist`() {
            every { studentRepository.findById(1) } returns Optional.empty()

            val exception = assertThrows<StudentNotFoundException> { cut.getStudent(1) }

            assertEquals("Student with id 1 not found", exception.message)
        }

    }

    @Nested
    inner class GetAllStudents {

        private val studentEntities = listOf(
            StudentEntity(1, "John", "Doe", "john.doe@example.com"),
            StudentEntity(2, "Jane", "Smith", "jane.smith@example.com")
        )

        @Test
        fun `returns correct StudentDtos`() {
            every { studentRepository.findAll() } returns studentEntities

            val result = cut.getAllStudents()

            assertEquals(studentEntities.map { it.toDto() }, result)
        }

    }

    @Nested
    inner class GetStudentsOfCourse {

        private val studentEntities = listOf(
            StudentEntity(1, "John", "Doe", "john.doe@example.com"),
            StudentEntity(2, "Jane", "Smith", "jane.smith@example.com")
        )

        @Test
        fun `returns correct StudentDtos`() {
            every { junctionService.getStudentIdsByCourseId(1) } returns listOf(1,2)
            every { studentRepository.findAllById(listOf(1,2)) } returns studentEntities

            val result = cut.getStudentsOfCourse(1)

            assertEquals(studentEntities.map { it.toDto() }, result)
        }

    }

    @Nested
    inner class EditStudent {

        private val updatedStudentDto = StudentDto(1, "Johnny", "Doe", "johnny.doe@example.com", listOf(1,2))
        private val oldStudentEntity = StudentEntity(1, "John", "Doe", "john.doe@example.com")
        private val updatedStudentEntity = StudentEntity(1, "Johnny", "Doe", "johnny.doe@example.com")

        @Test
        fun `returns correct StudentDto`() {
            every { studentRepository.findById(1) } returns Optional.of(oldStudentEntity)
            every { studentRepository.existsByEmail("johnny.doe@example.com") } returns false
            every { studentRepository.save(any()) } returns updatedStudentEntity
            every { junctionService.getCourseIdsByStudentId(1) } returns listOf(1,5)
            every { junctionService.addJunction(JunctionDto(1,2)) } just runs
            every { junctionService.removeJunction(JunctionDto(1,5)) } just runs

            val result = cut.editStudent(updatedStudentDto)

            assertEquals(updatedStudentEntity.toDto(), result)
        }

        @Test
        fun `persists data`() {
            val slot = slot<StudentEntity>()
            every { studentRepository.findById(1) } returns Optional.of(oldStudentEntity)
            every { studentRepository.existsByEmail("johnny.doe@example.com") } returns false
            every { studentRepository.save(capture(slot)) } returns updatedStudentEntity
            every { junctionService.getCourseIdsByStudentId(1) } returns listOf(1,5)
            every { junctionService.addJunction(JunctionDto(1,2)) } just runs
            every { junctionService.removeJunction(JunctionDto(1,5)) } just runs

            cut.editStudent(updatedStudentDto)

            assertEquals(updatedStudentDto.id, slot.captured.id)
            assertEquals(updatedStudentDto.firstName, slot.captured.firstName)
            assertEquals(updatedStudentDto.lastName, slot.captured.lastName)
            assertEquals(updatedStudentDto.email, slot.captured.email)
            verify { junctionService.addJunction(JunctionDto(1,2)) }
            verify { junctionService.removeJunction(JunctionDto(1,5)) }
        }

        @Test
        fun `fails when student does not exist`() {
            every { studentRepository.findById(1) } returns Optional.empty()

            val exception = assertThrows<StudentNotFoundException> { cut.editStudent(updatedStudentDto) }

            assertEquals("Student with id 1 not found", exception.message)
        }

        @Test
        fun `fails when email address is already taken`() {
            every { studentRepository.findById(1) } returns Optional.of(oldStudentEntity)
            every { studentRepository.existsByEmail("johnny.doe@example.com") } returns true

            val exception = assertThrows<StudentAlreadyExistsException> { cut.editStudent(updatedStudentDto) }

            assertEquals("Student with Email johnny.doe@example.com already exists", exception.message)
        }

    }

    @Nested
    inner class DeleteStudent {

        @Test
        fun `persists data`() {
            every { studentRepository.existsById(1) } returns true
            every { junctionService.deleteJunctionsByStudentId(1) } just runs
            every { studentRepository.deleteById(1) } just runs

            cut.deleteStudent(1)

            verify { studentRepository.existsById(1) }
            verify { junctionService.deleteJunctionsByStudentId(1) }
            verify { studentRepository.deleteById(1) }
        }

        @Test
        fun `fails when student does not exist`() {
            every { studentRepository.existsById(1) } returns false

            val exception = assertThrows<StudentNotFoundException> { cut.deleteStudent(1) }

            assertEquals("Student with id 1 not found", exception.message)
        }

    }

}