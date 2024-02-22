package com.example.studentcrmmonolith.dtos.course

import com.example.studentcrmmonolith.entities.CourseEntity

data class CourseDto(
    val id: Long,
    val name: String,
    val studentIds: List<Long>
) {
    fun toCourseResponseDto() = CourseResponseDto(id, name)

    fun toCourseEntity() = CourseEntity(id, name)
}
