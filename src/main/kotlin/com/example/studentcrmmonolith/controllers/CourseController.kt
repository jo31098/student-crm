package com.example.studentcrmmonolith.controllers

import com.example.studentcrmmonolith.dtos.course.AddCourseDto
import com.example.studentcrmmonolith.dtos.course.CourseResponseDto
import com.example.studentcrmmonolith.dtos.course.EditCourseDto
import com.example.studentcrmmonolith.services.CourseService
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
@RequestMapping("/course-api/v1")
@CrossOrigin(origins = ["http://localhost:3000"])
class CourseController(private val courseService: CourseService) {

    @PostMapping("/course")
    @ResponseStatus(HttpStatus.CREATED)
    fun addCourse(@RequestBody @Valid addCourseDto: AddCourseDto): CourseResponseDto {
        return courseService.addCourse(addCourseDto.toCourseDto()).toCourseResponseDto()
    }

    @GetMapping("/course/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getCourse(@PathVariable id: Long): CourseResponseDto {
        return courseService.getCourse(id).toCourseResponseDto()
    }

    @GetMapping("/courses")
    @ResponseStatus(HttpStatus.OK)
    fun getAllCourses(): List<CourseResponseDto> {
        return courseService.getAllCourses().map { it.toCourseResponseDto() }
    }

    @GetMapping("courses/of_student/{studentId}")
    @ResponseStatus(HttpStatus.OK)
    fun getCoursesOfStudent(@PathVariable studentId: Long): List<CourseResponseDto> {
        return courseService.getCoursesOfStudent(studentId).map { it.toCourseResponseDto() }
    }

    @PutMapping("/course")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun editCourse(@RequestBody @Valid editCourseDto: EditCourseDto): CourseResponseDto {
        return courseService.editCourse(editCourseDto.toCourseDto()).toCourseResponseDto()
    }

    @DeleteMapping("/course/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCourse(@PathVariable id: Long) {
        courseService.deleteCourse(id)
    }

}