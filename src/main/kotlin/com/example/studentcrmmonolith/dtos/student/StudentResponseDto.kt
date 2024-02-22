package com.example.studentcrmmonolith.dtos.student

data class StudentResponseDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String
)
