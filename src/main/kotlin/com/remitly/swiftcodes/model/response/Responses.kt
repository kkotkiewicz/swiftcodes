package com.remitly.swiftcodes.model.response

import com.fasterxml.jackson.annotation.JsonInclude

data class BankDetails(
    val address: String?,
    val bankName: String,
    val countryISO2: String,
    val countryName: String,
    val isHeadquarter: Boolean,
    val swiftCode: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val branches: List<BankBranchDetails>?,
)

data class BankBranchDetails(
    val address: String?,
    val bankName: String,
    val countryISO2: String,
    val isHeadquarter: Boolean,
    val swiftCode: String,
)

data class CountryBankDetails(
    val countryISO2: String,
    val countryName: String,
    val swiftCodes: List<BankBranchDetails>,
)

data class Message(
    val message: String,
)
