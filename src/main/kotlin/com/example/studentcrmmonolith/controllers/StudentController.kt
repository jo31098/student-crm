package com.example.studentcrmmonolith.controllers

import com.example.studentcrmmonolith.dtos.student.AddStudentDto
import com.example.studentcrmmonolith.dtos.student.EditStudentDto
import com.example.studentcrmmonolith.dtos.student.StudentResponseDto
import com.example.studentcrmmonolith.services.StudentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/student-api/v1")
@CrossOrigin(origins = ["http://localhost:3000"])
class StudentController(private val studentService: StudentService) {

    @PostMapping("/student")
    @ResponseStatus(HttpStatus.CREATED)
    fun addStudent(@RequestBody @Valid addStudentDto: AddStudentDto): StudentResponseDto {
        return studentService.addStudent(addStudentDto.toStudentDto()).toStudentResponseDto()
    }

    @GetMapping("/student/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getStudent(@PathVariable id: Long): StudentResponseDto {
        return studentService.getStudent(id).toStudentResponseDto()
    }

    @GetMapping("/students")
    @ResponseStatus(HttpStatus.OK)
    fun getAllStudents(): List<StudentResponseDto> {
        return studentService.getAllStudents().map { it.toStudentResponseDto() }
    }

    @GetMapping("/students/of_course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    fun getStudentsOfCourse(@PathVariable courseId: Long): List<StudentResponseDto> {
        return studentService.getStudentsOfCourse(courseId).map { it.toStudentResponseDto() }
    }

    @PutMapping("/student")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun editStudent(@RequestBody @Valid editStudentDto: EditStudentDto): StudentResponseDto {
        return studentService.editStudent(editStudentDto.toStudentDto()).toStudentResponseDto()
    }

    @DeleteMapping("/student/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStudent(@PathVariable id: Long) {
        studentService.deleteStudent(id)
    }

}