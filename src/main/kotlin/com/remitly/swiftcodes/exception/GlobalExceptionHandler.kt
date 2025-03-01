package com.remitly.swiftcodes.exception

import com.remitly.swiftcodes.model.response.ErrorResponse
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
            ErrorResponse("Validation failed", errors),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.associate { it.propertyPath.toString() to it.message }
        return ResponseEntity(
            ErrorResponse("Validation failed", errors),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(BankNotFoundException::class)
    fun handleBankNotFoundException(ex: BankNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Bank not found"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(BankWithSwiftCodeExists::class)
    fun handleBankWithSwiftCodeExists(ex: BankWithSwiftCodeExists): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Bank already exists"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(InvalidSwiftCodeException::class)
    fun handleInvalidSwiftCodeException(ex: InvalidSwiftCodeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Invalid SWIFT code"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(InvalidCountryCodeException::class)
    fun handleInvalidCountryCodeException(ex: InvalidCountryCodeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Invalid country ISO2 code"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(HeadquartersMismatchException::class)
    fun handleHeadquartersMismatchException(ex: HeadquartersMismatchException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Headquarters mismatch"),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(CannotDeleteHeadquartersException::class)
    fun handleCannotDeleteHeadquartersException(ex: CannotDeleteHeadquartersException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Cannot delete headquarters"),
            HttpStatus.CONFLICT
        )
    }
}
