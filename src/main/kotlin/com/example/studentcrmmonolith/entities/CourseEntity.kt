package com.example.studentcrmmonolith.entities

import com.example.studentcrmmonolith.dtos.course.CourseDto
import jakarta.persistence.*

@Entity
@Table(name = "courses")
class CourseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "name", nullable = false, unique = true, length = 35)
    val name: String

) {

    fun toDto() = CourseDto(id, name, emptyList())

}