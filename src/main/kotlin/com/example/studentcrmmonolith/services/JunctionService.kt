package com.example.studentcrmmonolith.services

import com.example.studentcrmmonolith.dtos.JunctionDto
import com.example.studentcrmmonolith.entities.JunctionEntity
import com.example.studentcrmmonolith.exceptions.CourseNotFoundException
import com.example.studentcrmmonolith.exceptions.StudentNotFoundException
import com.example.studentcrmmonolith.repositories.CourseRepository
import com.example.studentcrmmonolith.repositories.JunctionRepository
import com.example.studentcrmmonolith.repositories.StudentRepository
import org.springframework.stereotype.Service

@Service
class JunctionService(
    private val junctionRepository: JunctionRepository,
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository
) {

    fun getStudentIdsByCourseId(courseId: Long): List<Long> {
        return junctionRepository.findAllByCourseId(courseId).map { it.studentId }
    }

    fun getCourseIdsByStudentId(studentId: Long): List<Long> {
        return junctionRepository.findAllByStudentId(studentId).map { it.courseId }
    }

    fun addJunction(junctionDto: JunctionDto) {
        if (!studentRepository.existsById(junctionDto.studentId)) {
            throw StudentNotFoundException(junctionDto.studentId)
        }
        if (!courseRepository.existsById(junctionDto.courseId)) {
            throw CourseNotFoundException(junctionDto.courseId)
        }
        junctionRepository.save(JunctionEntity(0, junctionDto.studentId, junctionDto.courseId))
    }

    fun removeJunction(junctionDto: JunctionDto) {
        junctionRepository.deleteByStudentIdAndCourseId(junctionDto.studentId, junctionDto.courseId)
    }

    fun deleteJunctionsByStudentId(studentId: Long) {
        junctionRepository.deleteAllByStudentId(studentId)
    }

    fun deleteJunctionsByCourseId(courseId: Long) {
        junctionRepository.deleteAllByCourseId(courseId)
    }

}