package com.example.studentcrmmonolith.services

import com.example.studentcrmmonolith.dtos.JunctionDto
import com.example.studentcrmmonolith.dtos.course.CourseDto
import com.example.studentcrmmonolith.entities.CourseEntity
import com.example.studentcrmmonolith.exceptions.CourseAlreadyExistsException
import com.example.studentcrmmonolith.exceptions.CourseNotFoundException
import com.example.studentcrmmonolith.repositories.CourseRepository
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

class CourseServiceTest {

    private val courseRepository = mockk<CourseRepository>()
    private val junctionService = mockk<JunctionService>()

    private val cut = CourseService(courseRepository, junctionService)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    inner class AddCourse {

        private val courseDto = CourseDto(0, "Math", listOf(1,2))
        private val courseEntity = CourseEntity(1, "Math")

        @Test
        fun `returns correct CourseDto`() {
            every { courseRepository.existsByName("Math") } returns false
            every { courseRepository.save(any()) } returns courseEntity
            every { junctionService.addJunction(any()) } just runs

            val result = cut.addCourse(courseDto)

            assertEquals(courseEntity.toDto(), result)
        }

        @Test
        fun `persists Data`() {
            val slot = slot<CourseEntity>()
            every { courseRepository.existsByName("Math") } returns false
            every { courseRepository.save(capture(slot)) } returns courseEntity
            every { junctionService.addJunction(any()) } just runs

            cut.addCourse(courseDto)

            assertEquals(courseDto.id, slot.captured.id)
            assertEquals(courseDto.name, slot.captured.name)
            verify { junctionService.addJunction(JunctionDto(1,1)) }
            verify { junctionService.addJunction(JunctionDto(2,1)) }
        }

        @Test
        fun `fails when course already exists`() {
            every { courseRepository.existsByName("Math") } returns true

            val exception = assertThrows<CourseAlreadyExistsException> { cut.addCourse(courseDto) }

            assertEquals("Course with name Math already exists", exception.message)
        }

    }

    @Nested
    inner class GetCourse {

        private val courseEntity = CourseEntity(1, "Math")

        @Test
        fun `returns correct CourseDto`() {
            every { courseRepository.findById(1) } returns Optional.of(courseEntity)

            val result = cut.getCourse(1)

            assertEquals(courseEntity.toDto(), result)
        }

        @Test
        fun `fails when course does not exist`() {
            every { courseRepository.findById(1) } returns Optional.empty()

            val exception = assertThrows<CourseNotFoundException> { cut.getCourse(1) }

            assertEquals("Course with id 1 not found", exception.message)
        }

    }

    @Nested
    inner class GetAllCourses {

        private val courseEntities = listOf(
            CourseEntity(1, "Math"),
            CourseEntity(2, "Physics")
        )

        @Test
        fun `returns correct CourseDtos`() {
            every { courseRepository.findAll() } returns courseEntities

            val result = cut.getAllCourses()

            assertEquals(courseEntities.map { it.toDto() }, result)
        }

    }

    @Nested
    inner class GetCoursesOfStudent {

        private val courseEntities = listOf(
            CourseEntity(1, "Math"),
            CourseEntity(2, "Physics")
        )

        @Test
        fun `returns correct CourseDtos`() {
            every { junctionService.getCourseIdsByStudentId(1) } returns listOf(1,2)
            every { courseRepository.findAllById(listOf(1,2)) } returns courseEntities

            val result = cut.getCoursesOfStudent(1)

            assertEquals(courseEntities.map { it.toDto() }, result)
        }

    }

    @Nested
    inner class EditCourse {

        private val updatedCourseDto = CourseDto(1, "Mathematics", listOf(1,2))
        private val oldCourseEntity = CourseEntity(1, "Math")
        private val updatedCourseEntity = CourseEntity(1, "Mathematics")

        @Test
        fun `returns correct CourseDto`() {
            every { courseRepository.findById(1) } returns Optional.of(oldCourseEntity)
            every { courseRepository.existsByName("Mathematics") } returns false
            every { courseRepository.save(any()) } returns updatedCourseEntity
            every { junctionService.getStudentIdsByCourseId(1) } returns listOf(1,5)
            every { junctionService.addJunction(JunctionDto(2, 1)) } just runs
            every { junctionService.removeJunction(JunctionDto(5,1)) } just runs

            val result = cut.editCourse(updatedCourseDto)

            assertEquals(updatedCourseEntity.toDto(), result)
        }

        @Test
        fun `persists data`() {
            val slot = slot<CourseEntity>()
            every { courseRepository.findById(1) } returns Optional.of(oldCourseEntity)
            every { courseRepository.existsByName("Mathematics") } returns false
            every { courseRepository.save(capture(slot)) } returns updatedCourseEntity
            every { junctionService.getStudentIdsByCourseId(1) } returns listOf(1,5)
            every { junctionService.addJunction(JunctionDto(2, 1)) } just runs
            every { junctionService.removeJunction(JunctionDto(5,1)) } just runs

            cut.editCourse(updatedCourseDto)

            assertEquals(updatedCourseDto.id, slot.captured.id)
            assertEquals(updatedCourseDto.name, slot.captured.name)
            verify { junctionService.addJunction(JunctionDto(2, 1)) }
            verify { junctionService.removeJunction(JunctionDto(5,1)) }
        }

        @Test
        fun `fails when course does not exist`() {
            every { courseRepository.findById(1) } returns Optional.empty()

            val exception = assertThrows<CourseNotFoundException> { cut.editCourse(updatedCourseDto) }

            assertEquals("Course with id 1 not found", exception.message)
        }

        @Test
        fun `fails when course already exists`() {
            every { courseRepository.findById(1) } returns Optional.of(oldCourseEntity)
            every { courseRepository.existsByName("Mathematics") } returns true

            val exception = assertThrows<CourseAlreadyExistsException> { cut.editCourse(updatedCourseDto) }

            assertEquals("Course with name Mathematics already exists", exception.message)
        }

    }

    @Nested
    inner class DeleteCourse {

        @Test
        fun `persists data`() {
            every { courseRepository.existsById(1) } returns true
            every { junctionService.deleteJunctionsByCourseId(1) } just runs
            every { courseRepository.deleteById(1) } just runs

            cut.deleteCourse(1)

            verify { courseRepository.existsById(1) }
            verify { junctionService.deleteJunctionsByCourseId(1) }
            verify { courseRepository.deleteById(1) }
        }

        @Test
        fun `fails when course does not exist`() {
            every { courseRepository.existsById(1) } returns false

            val exception = assertThrows<CourseNotFoundException> { cut.deleteCourse(1) }

            assertEquals("Course with id 1 not found", exception.message)
        }

    }

}