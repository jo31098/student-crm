package com.example.studentcrmmonolith.repositories

import com.example.studentcrmmonolith.entities.StudentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StudentRepository: JpaRepository<StudentEntity, Long> {

    fun existsByEmail(email: String): Boolean

}