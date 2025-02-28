package com.remitly.swiftcodes.entity

import com.remitly.swiftcodes.BankBranchDetails
import com.remitly.swiftcodes.BankDetails
import jakarta.persistence.*

@Entity
class HeadquartersEntity(
    @Id
    @Column(length = 11)
    var swiftCode: String,
    var bankName: String,
    var countryISO2: String,
    var countryName: String,
    var address: String?,
    @OneToMany(mappedBy = "headquartersId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var branches: MutableList<BranchEntity> = mutableListOf()
) {


    fun toBankDetails(): BankDetails = BankDetails(
        swiftCode = swiftCode,
        bankName = bankName,
        countryName = countryName,
        countryISO2 = countryISO2,
        isHeadquarter = true,
        address = address,
        branches = branches.map { it.toBankBranchDetails() },
    )

    fun toBankBranchDetails(): BankBranchDetails = BankBranchDetails(
        swiftCode = swiftCode,
        bankName = bankName,
        countryISO2 = countryISO2,
        isHeadquarter = true,
        address = address,
    )
}