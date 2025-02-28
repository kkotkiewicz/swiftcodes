package com.remitly.swiftcodes.entity

import com.remitly.swiftcodes.BankBranchDetails
import com.remitly.swiftcodes.BankDetails
import jakarta.persistence.*

@Entity
class BranchEntity(
    @Id
    @Column(length = 11)
    var swiftCode: String,
    var bankName: String,
    var countryISO2: String,
    var countryName: String,
    var address: String?,
    @Column(name = "headquarters_swift_code", nullable = false)
    var headquartersId: String,
) {
    fun toBankDetails(): BankDetails = BankDetails(
        swiftCode = swiftCode,
        bankName = bankName,
        countryName = countryName,
        countryISO2 = countryISO2,
        isHeadquarter = false,
        address = address,
        branches = null,
    )

    fun toBankBranchDetails(): BankBranchDetails = BankBranchDetails(
        swiftCode = swiftCode,
        bankName = bankName,
        countryISO2 = countryISO2,
        isHeadquarter = false,
        address = address,
    )
}