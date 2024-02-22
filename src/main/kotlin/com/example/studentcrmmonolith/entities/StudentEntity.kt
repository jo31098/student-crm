package com.example.studentcrmmonolith.entities

import com.example.studentcrmmonolith.dtos.student.StudentDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "students")
class StudentEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "first_name", nullable = false, length = 20)
    val firstName: String,

    @Column(name = "last_name", nullable = false, length = 20)
    val lastName: String,

    @Column(name = "email", nullable = false, unique = true)
    val email: String

) {

    fun toDto() = StudentDto(id, firstName, lastName, email, emptyList())

}