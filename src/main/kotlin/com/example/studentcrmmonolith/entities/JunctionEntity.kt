package com.example.studentcrmmonolith.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "students_courses")
class JunctionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "student_id", nullable = false, unique = true)
    val studentId: Long,

    @Column(name = "course_id", nullable = false, unique = true)
    val courseId: Long

)