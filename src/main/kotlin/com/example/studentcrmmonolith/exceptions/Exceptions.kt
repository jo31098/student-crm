package com.example.studentcrmmonolith.exceptions

sealed class ClientError(message: String) : RuntimeException(message)

class StudentNotFoundException(id: Long) : ClientError("Student with id $id not found")

class StudentAlreadyExistsException(email: String) : ClientError("Student with Email $email already exists")

class CourseNotFoundException(id: Long) : ClientError("Course with id $id not found")

class CourseAlreadyExistsException(name: String) : ClientError("Course with name $name already exists")
