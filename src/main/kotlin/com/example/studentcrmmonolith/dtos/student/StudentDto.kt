package com.example.studentcrmmonolith.dtos.student

import com.example.studentcrmmonolith.entities.StudentEntity

data class StudentDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val courseIds: List<Long>
) {
    fun toStudentResponseDto() = StudentResponseDto(id, firstName, lastName, email)

    fun toStudentEntity() = StudentEntity(id, firstName, lastName, email)
}
