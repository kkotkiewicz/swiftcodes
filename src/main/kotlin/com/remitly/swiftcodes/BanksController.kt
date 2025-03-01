package com.remitly.swiftcodes

import com.remitly.swiftcodes.model.BankDetails
import com.remitly.swiftcodes.model.CountryBankDetails
import com.remitly.swiftcodes.model.Message
import com.remitly.swiftcodes.model.dto.BankDetailsDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/v1/swift-codes")
@RestController
class SwiftCodesController(
    private val banksService: BanksService
) {
    @GetMapping("/{swiftCode}")
    fun getBankDetails(
        @PathVariable
        @Pattern(regexp = "^[A-Z0-9]{11}$", message = "Invalid SWIFT code format (must be 11 capital letters)")
        swiftCode: String
    ): BankDetails {
        return banksService.getBankDetails(swiftCode)
    }

    @GetMapping("/country/{countryISO2}")
    fun getCountryBanksDetails(
        @PathVariable
        @Pattern(regexp = "^[A-Z]{2}$", message = "Invalid country ISO2 code (must be 2 capital letters)")
        countryISO2: String
    ): CountryBankDetails {
        return banksService.getBankDetailsByCountry(countryISO2)
    }

    @PostMapping
    fun saveBankDetails(@Valid @RequestBody bankDetails: BankDetailsDto): Message {
        return banksService.saveBankDetails(bankDetails)
    }

    @DeleteMapping("/{swiftCode}")
    fun deleteBankDetails(
        @PathVariable
        @Pattern(regexp = "^[A-Z0-9]{11}$", message = "Invalid SWIFT code format (must be 11 capital letters)")
        swiftCode: String
    ): Message {
        return banksService.deleteBankDetails(swiftCode)
    }
}
