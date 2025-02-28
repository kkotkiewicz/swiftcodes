package com.remitly.swiftcodes.repository

import com.remitly.swiftcodes.entity.BranchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BranchRepository: JpaRepository<BranchEntity, String> {
    fun findAllByCountryISO2(countryISO2: String): List<BranchEntity>

    fun findAllByHeadquartersId(swiftCode: String): List<BranchEntity>
}