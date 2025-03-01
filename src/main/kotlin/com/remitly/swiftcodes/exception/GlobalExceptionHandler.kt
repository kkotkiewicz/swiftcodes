package com.remitly.swiftcodes.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.associate {
            (it as FieldError).field to it.defaultMessage
        }
        return ResponseEntity(
            ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.associate { it.propertyPath.toString() to it.message }
        return ResponseEntity(
            ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(BankNotFoundException::class)
    fun handleBankNotFoundException(ex: BankNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.message ?: "Bank not found", emptyMap()),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(BankWithSwiftCodeExists::class)
    fun handleBankExistsException(ex: BankNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "Bank already exists", emptyMap()),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(InvalidSwiftCodeException::class)
    fun handleInvalidSwiftCodeException(ex: InvalidSwiftCodeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "Invalid SWIFT code", emptyMap()),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(InvalidCountryCodeException::class)
    fun handleInvalidCountryCodeException(ex: InvalidCountryCodeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "Invalid country ISO2 code", emptyMap()),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(HeadquartersMismatchException::class)
    fun handleHeadquartersMismatchException(ex: HeadquartersMismatchException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(HttpStatus.CONFLICT.value(), ex.message ?: "Headquarters mismatch", emptyMap()),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(CannotDeleteHeadquartersException::class)
    fun handleCannotDeleteHeadquartersException(ex: CannotDeleteHeadquartersException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(HttpStatus.CONFLICT.value(), ex.message ?: "Cannot delete headquarters", emptyMap()),
            HttpStatus.CONFLICT
        )
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: Map<String, String?>
)
