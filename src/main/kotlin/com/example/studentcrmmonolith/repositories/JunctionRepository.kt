package com.example.studentcrmmonolith.repositories

import com.example.studentcrmmonolith.entities.JunctionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JunctionRepository: JpaRepository<JunctionEntity, Long> {

    fun findAllByStudentId(studentId: Long): List<JunctionEntity>

    fun findAllByCourseId(courseId: Long): List<JunctionEntity>

    fun deleteByStudentIdAndCourseId(studentId: Long, courseId: Long)

    fun deleteAllByStudentId(studentId: Long)

    fun deleteAllByCourseId(courseId: Long)

}