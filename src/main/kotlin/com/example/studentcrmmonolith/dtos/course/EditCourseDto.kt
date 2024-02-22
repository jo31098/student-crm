package com.example.studentcrmmonolith.dtos.course

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class EditCourseDto(

    @field:NotNull(message = "ID is required")
    val id: Long,

    @field:NotNull(message = "Course Name is required")
    @field:NotBlank(message = "Course Name is required")
    @field:Size(max = 35, message = "Course Name must be less than 35 characters")
    val name: String,

    @field:NotNull(message =
    "List of Student-IDs is required. Enter an empty list if no students should be assigned to this course")
    val studentIds: List<Long>

) {
    fun toCourseDto() = CourseDto(id, name, studentIds)
}
