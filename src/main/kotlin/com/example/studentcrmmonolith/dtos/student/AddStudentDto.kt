package com.example.studentcrmmonolith.dtos.student

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class AddStudentDto(

    @field:NotNull(message = "First Name is required")
    @field:NotBlank(message = "First Name is required")
    @field:Size(max = 20, message = "First Name must be less than 20 characters")
    val firstName: String,

    @field:NotNull(message = "Last Name is required")
    @field:NotBlank(message = "Last Name is required")
    @field:Size(max = 20, message = "Last Name must be less than 20 characters")
    val lastName: String,

    @field:NotNull(message = "Email is required")
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email format is invalid", regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}")
    val email: String,

    @field:NotNull(
        message = "List of Course-IDs is required. Enter an empty list if no courses should be assigned to this student"
    )
    val courseIds: List<Long>

) {
    fun toStudentDto() = StudentDto(0, firstName, lastName, email, courseIds)
}
