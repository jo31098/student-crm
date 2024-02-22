package com.example.studentcrmmonolith.exceptions

import com.example.studentcrmmonolith.dtos.ExceptionDto
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: HttpServletRequest): ExceptionDto {
        logger.info("An incoming HTTP request caused an error and was rejected (Endpoint: ${request.method} ${request.requestURI} | Error: ${ex.message})")
        return ExceptionDto(ex.bindingResult.fieldErrors.last().defaultMessage ?: "Validation Error")
    }

    @ExceptionHandler(ClientError::class)
    fun handleRequestExceptions(ex: ClientError, request: HttpServletRequest): ResponseEntity<ExceptionDto> {
        logger.warn("An incoming HTTP request caused an error and was rejected (Endpoint: ${request.method} ${request.requestURI} | Error: ${ex.message})")
        return when (ex) {
            is StudentAlreadyExistsException, is CourseAlreadyExistsException -> {
                ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionDto(ex.message ?: "Validation Error"))
            }

            is StudentNotFoundException, is CourseNotFoundException -> {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionDto(ex.message ?: "Validation Error"))
            }
        }
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleOtherExceptions(ex: Exception): ExceptionDto {
        logger.error("An internal error occurred (Error Message: ${ex.message})")
        return ExceptionDto("An internal error occurred")
    }

}