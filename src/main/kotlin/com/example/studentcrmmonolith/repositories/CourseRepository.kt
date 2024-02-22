package com.example.studentcrmmonolith.repositories

import com.example.studentcrmmonolith.entities.CourseEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository: JpaRepository<CourseEntity, Long> {

    fun existsByName(name: String): Boolean

}