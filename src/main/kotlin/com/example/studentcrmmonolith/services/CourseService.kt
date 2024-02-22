package com.example.studentcrmmonolith.services

import com.example.studentcrmmonolith.dtos.JunctionDto
import com.example.studentcrmmonolith.dtos.course.CourseDto
import com.example.studentcrmmonolith.entities.CourseEntity
import com.example.studentcrmmonolith.exceptions.CourseAlreadyExistsException
import com.example.studentcrmmonolith.exceptions.CourseNotFoundException
import com.example.studentcrmmonolith.repositories.CourseRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CourseService(
    private val courseRepository: CourseRepository,
    private val junctionService: JunctionService
) {

    fun addCourse(courseDto: CourseDto): CourseDto {
        if (courseRepository.existsByName(courseDto.name)) {
            throw CourseAlreadyExistsException(courseDto.name)
        }
        val addedCourse = courseRepository.save(courseDto.toCourseEntity())
        courseDto.studentIds.forEach { studentId ->
            junctionService.addJunction(JunctionDto(studentId, addedCourse.id))
        }
        return addedCourse.toDto()
    }

    fun getCourse(id: Long): CourseDto {
        return courseRepository.findById(id).getOrNull()
            ?.toDto()
            ?: throw CourseNotFoundException(id)
    }

    fun getAllCourses(): List<CourseDto> {
        return courseRepository.findAll().map { it.toDto() }
    }

    fun getCoursesOfStudent(studentId: Long): List<CourseDto> {
        val courseIds = junctionService.getCourseIdsByStudentId(studentId)
        return courseRepository.findAllById(courseIds).map { it.toDto() }
    }

    fun editCourse(courseDto: CourseDto): CourseDto {
        val oldCourse = courseRepository.findById(courseDto.id).getOrNull()
            ?: throw CourseNotFoundException(courseDto.id)
        if (courseDto.name != oldCourse.name && courseRepository.existsByName(courseDto.name)) {
            throw CourseAlreadyExistsException(courseDto.name)
        }
        val updatedCourse = courseRepository.save(courseDto.toCourseEntity())
        val studentIdsToBeRemoved = junctionService.getStudentIdsByCourseId(courseDto.id).toMutableList()
        val studentIdsToBeAdded = mutableListOf<Long>()
        for (id in courseDto.studentIds) {
            if (studentIdsToBeRemoved.contains(id)) {
                studentIdsToBeRemoved.remove(id)
            }
            else {
                studentIdsToBeAdded.add(id)
            }
        }
        studentIdsToBeAdded.forEach { studentId ->
            junctionService.addJunction(JunctionDto(studentId, courseDto.id))
        }
        studentIdsToBeRemoved.forEach { studentId ->
            junctionService.removeJunction(JunctionDto(studentId, courseDto.id))
        }
        return updatedCourse.toDto()
    }

    fun deleteCourse(id: Long) {
        if (!courseRepository.existsById(id)) {
            throw CourseNotFoundException(id)
        }
        junctionService.deleteJunctionsByCourseId(id)
        courseRepository.deleteById(id)
    }

}