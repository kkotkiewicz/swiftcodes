package com.remitly.swiftcodes

import com.remitly.swiftcodes.model.dto.BankDetailsDto
import com.remitly.swiftcodes.model.entity.BranchEntity
import com.remitly.swiftcodes.model.entity.HeadquartersEntity


fun getBankDetailsDto(
    swiftCode: String,
    isHeadquarter: Boolean,
    address: String = "Some Address",
    bankName: String = "Pekao",
    countryISO2: String = "PL",
    countryName: String = "Poland",
) = BankDetailsDto(
    swiftCode = swiftCode,
    isHeadquarter = isHeadquarter,
    address = address,
    bankName = bankName,
    countryISO2 = countryISO2,
    countryName = countryName
)

fun getBranchEntity(
    swiftCode: String,
    bankName: String = "Pekao",
    countryISO2: String = "PL",
    countryName: String = "Poland",
    address: String? = null,
    headquarters: HeadquartersEntity,
) = BranchEntity(
    swiftCode = swiftCode,
    bankName = bankName,
    countryISO2 = countryISO2,
    countryName = countryName,
    address = address,
    headquarters = headquarters
)

fun getHeadquartersEntity(
    swiftCode: String,
    bankName: String = "Pekao",
    countryISO2: String = "PL",
    countryName: String = "Poland",
    address: String? = null,
    branches: MutableList<BranchEntity> = mutableListOf(),
) = HeadquartersEntity(
    swiftCode = swiftCode,
    bankName = bankName,
    countryISO2 = countryISO2,
    countryName = countryName,
    address = address,
    branches = branches
)
