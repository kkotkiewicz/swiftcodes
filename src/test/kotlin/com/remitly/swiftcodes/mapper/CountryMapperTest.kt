package com.remitly.swiftcodes.mapper

import com.remitly.swiftcodes.exception.InvalidCountryCodeException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CountryMapperTest() {
    private val countryMapper = CountryMapper()

    @Test
    fun `should return correct country name for valid ISO2 code`() {
        val result = countryMapper.getCountryName("US")
        assertEquals("United States", result)
    }

    @Test
    fun `should return correct country name for lowercase ISO2 code`() {
        val result = countryMapper.getCountryName("gb")
        assertEquals("United Kingdom", result)
    }

    @Test
    fun `should throw InvalidCountryCodeException for invalid ISO2 code`() {
        val invalidCode = "XX"

        val exception = assertThrows<InvalidCountryCodeException> {
            countryMapper.getCountryName(invalidCode)
        }

        assertEquals("Invalid country ISO2 code: XX", exception.message)
    }

    @Test
    fun `should throw InvalidCountryCodeException for empty ISO2 code`() {
        val exception = assertThrows<InvalidCountryCodeException> {
            countryMapper.getCountryName("")
        }

        assertEquals("Invalid country ISO2 code: ", exception.message)
    }
}
