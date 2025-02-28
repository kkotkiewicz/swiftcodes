package com.remitly.swiftcodes

import com.remitly.swiftcodes.entity.BranchEntity
import com.remitly.swiftcodes.entity.HeadquartersEntity
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
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
        @Pattern(regexp = "^[A-Z]{11}$", message = "Invalid SWIFT code format (must be 11 capital letters)")
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
        @Pattern(regexp = "^[A-Z]{11}$", message = "Invalid SWIFT code format (must be 11 capital letters)")
        swiftCode: String
    ): Message {
        return banksService.deleteBankDetails(swiftCode)
    }
}

data class BankDetailsDto(
    val address: String? = null,

    @field:NotBlank(message = "Bank name is required")
    val bankName: String,

    @field:NotBlank(message = "Country ISO2 code is required")
    @field:Pattern(regexp = "^[A-Z]{2}$", message = "Country ISO2 code must be exactly 2 capital letters")
    val countryISO2: String,

    @field:NotBlank(message = "Country name is required")
    val countryName: String,

    val isHeadquarter: Boolean,

    @field:NotBlank(message = "SWIFT code is required")
    @field:Pattern(regexp = "^[A-Z]{11}$", message = "SWIFT code must be exactly 11 capital letters")
    val swiftCode: String
) {
    fun toHeadquartersEntity(branches: MutableList<BranchEntity>) = HeadquartersEntity(
        swiftCode = swiftCode,
        countryISO2 = countryISO2,
        countryName = countryName,
        bankName = bankName,
        address = address,
        branches = branches,
    )

    fun toBranchEntity(headquartersSwiftCode: String) = BranchEntity(
        swiftCode = swiftCode,
        countryISO2 = countryISO2,
        countryName = countryName,
        bankName = bankName,
        address = address,
        headquartersId = headquartersSwiftCode
    )

    fun toBranchBankDetails() = BankBranchDetails(
        swiftCode = swiftCode,
        countryISO2 = countryISO2,
        bankName = bankName,
        address = address,
        isHeadquarter = isHeadquarter,
    )
}
