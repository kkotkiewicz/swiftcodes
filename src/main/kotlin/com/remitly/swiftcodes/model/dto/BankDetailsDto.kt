package com.remitly.swiftcodes.model.dto

import com.remitly.swiftcodes.model.BankBranchDetails
import com.remitly.swiftcodes.model.entity.BranchEntity
import com.remitly.swiftcodes.model.entity.HeadquartersEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

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
    @field:Pattern(regexp = "^[A-Z0-9]{11}$", message = "SWIFT code must be exactly 11 capital letters")
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

    fun toBranchEntity(headquartersEntity: HeadquartersEntity) = BranchEntity(
        swiftCode = swiftCode,
        countryISO2 = countryISO2,
        countryName = countryName,
        bankName = bankName,
        address = address,
        headquarters = headquartersEntity
    )
}