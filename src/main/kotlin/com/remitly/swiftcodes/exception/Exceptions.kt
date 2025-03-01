package com.remitly.swiftcodes.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class BankNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class BankWithSwiftCodeExists(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidSwiftCodeException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidCountryCodeException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class HeadquartersMismatchException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class CannotDeleteHeadquartersException(message: String) : RuntimeException(message)
