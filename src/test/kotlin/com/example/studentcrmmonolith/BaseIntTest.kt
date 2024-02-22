package com.example.studentcrmmonolith

import com.example.studentcrmmonolith.repositories.CourseRepository
import com.example.studentcrmmonolith.repositories.JunctionRepository
import com.example.studentcrmmonolith.repositories.StudentRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
abstract class BaseIntTest {

    @Autowired
    private lateinit var studentRepository: StudentRepository
    @Autowired
    private lateinit var courseRepository: CourseRepository
    @Autowired
    private lateinit var junctionRepository: JunctionRepository

    @BeforeEach
    fun cleanDatabase() {
        junctionRepository.deleteAll()
        studentRepository.deleteAll()
        courseRepository.deleteAll()
    }

}