package com.example.studentcrmmonolith.services

import com.example.studentcrmmonolith.dtos.JunctionDto
import com.example.studentcrmmonolith.dtos.student.StudentDto
import com.example.studentcrmmonolith.entities.StudentEntity
import com.example.studentcrmmonolith.exceptions.StudentAlreadyExistsException
import com.example.studentcrmmonolith.exceptions.StudentNotFoundException
import com.example.studentcrmmonolith.repositories.StudentRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class StudentService(
    private val studentRepository: StudentRepository,
    private val junctionService: JunctionService
) {

    fun addStudent(studentDto: StudentDto): StudentDto {
        if (studentRepository.existsByEmail(studentDto.email)) {
            throw StudentAlreadyExistsException(studentDto.email)
        }
        val addedStudent = studentRepository.save(studentDto.toStudentEntity())
        studentDto.courseIds.forEach { courseId ->
            junctionService.addJunction(JunctionDto(addedStudent.id, courseId))
        }
        return addedStudent.toDto()
    }

    fun getStudent(id: Long): StudentDto {
        return studentRepository.findById(id).getOrNull()
            ?.toDto()
            ?: throw StudentNotFoundException(id)
    }

    fun getAllStudents(): List<StudentDto> {
        return studentRepository.findAll().map { it.toDto() }
    }

    fun getStudentsOfCourse(courseId: Long): List<StudentDto> {
        val studentsIds = junctionService.getStudentIdsByCourseId(courseId)
        return studentRepository.findAllById(studentsIds).map { it.toDto() }
    }

    fun editStudent(studentDto: StudentDto): StudentDto {
        val oldStudent = studentRepository.findById(studentDto.id).getOrNull()
            ?: throw StudentNotFoundException(studentDto.id)
        if (studentDto.email != oldStudent.email && studentRepository.existsByEmail(studentDto.email)) {
            throw StudentAlreadyExistsException(studentDto.email)
        }
        val updatedStudent = studentRepository.save(studentDto.toStudentEntity())
        val courseIdsToBeRemoved = junctionService.getCourseIdsByStudentId(studentDto.id).toMutableList()
        val courseIdsToBeAdded = mutableListOf<Long>()
        for (id in studentDto.courseIds) {
            if (courseIdsToBeRemoved.contains(id)) {
                courseIdsToBeRemoved.remove(id)
            }
            else {
                courseIdsToBeAdded.add(id)
            }
        }
        courseIdsToBeAdded.forEach { courseId ->
            junctionService.addJunction(JunctionDto(studentDto.id, courseId))
        }
        courseIdsToBeRemoved.forEach { courseId ->
            junctionService.removeJunction(JunctionDto(studentDto.id, courseId))
        }
        return updatedStudent.toDto()
    }

    fun deleteStudent(id: Long) {
        if (!studentRepository.existsById(id)) {
            throw StudentNotFoundException(id)
        }
        junctionService.deleteJunctionsByStudentId(id)
        studentRepository.deleteById(id)
    }

}