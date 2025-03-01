package com.remitly.swiftcodes.model.entity

import com.remitly.swiftcodes.model.response.BankBranchDetails
import com.remitly.swiftcodes.model.response.BankDetails
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Entity

@Entity
class BranchEntity(
    @Id
    @Column(length = 11)
    var swiftCode: String,
    var bankName: String,
    var countryISO2: String,
    var countryName: String,
    var address: String?,
    @ManyToOne(optional = false)
    @JoinColumn(name = "headquarters_swift_code", nullable = false)
    var headquarters: HeadquartersEntity,
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