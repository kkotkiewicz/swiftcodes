package com.remitly.swiftcodes.repository

import com.remitly.swiftcodes.entity.HeadquartersEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HeadquartersRepository: JpaRepository<HeadquartersEntity, String> {
    fun findAllByCountryISO2(countryISO2: String): List<HeadquartersEntity>
}