package com.remitly.swiftcodes

import com.remitly.swiftcodes.mapper.CountryMapper
import com.remitly.swiftcodes.exception.BankNotFoundException
import com.remitly.swiftcodes.exception.HeadquartersMismatchException
import com.remitly.swiftcodes.exception.BankWithSwiftCodeExists
import com.remitly.swiftcodes.exception.InvalidCountryCodeException
import com.remitly.swiftcodes.exception.CannotDeleteHeadquartersException
import com.remitly.swiftcodes.model.entity.BranchEntity
import com.remitly.swiftcodes.model.entity.HeadquartersEntity
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import io.mockk.mockk
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import kotlin.test.assertEquals

class BanksServiceTest {
    private val headquartersRepository = mockk<HeadquartersRepository>(relaxed = true)
    private val branchRepository = mockk<BranchRepository>(relaxed = true)
    private val countryMapper = mockk<CountryMapper>{ every { getCountryName("PL") } returns "Poland" }
    private val banksService: BanksService = BanksService(headquartersRepository, branchRepository, countryMapper)

    @Test
    fun `should return bank details when SWIFT code exists in headquarters`() {
        val swiftCode = "ABCDEF12XXX"
        val headquartersEntity = getHeadquartersEntity(swiftCode)

        every { headquartersRepository.findById(swiftCode) } returns Optional.of(headquartersEntity)

        val result = banksService.getBankDetails(swiftCode)

        assertEquals(headquartersEntity.toBankDetails(), result)
        verify { headquartersRepository.findById(swiftCode) }
    }

    @Test
    fun `should return bank details when SWIFT code exists in branches`() {
        val swiftCode = "ABCDEF12BRN"
        val headquartersEntity = getHeadquartersEntity(swiftCode)
        val branchEntity = getBranchEntity(swiftCode, headquarters = headquartersEntity)

        every { branchRepository.findById(swiftCode) } returns Optional.of(branchEntity)

        val result = banksService.getBankDetails(swiftCode)

        assertEquals(branchEntity.toBankDetails(), result)
        verify { branchRepository.findById(swiftCode) }
    }

    @Test
    fun `should throw BankNotFoundException when headquarters SWIFT code not found in headquarters`() {
        val swiftCode = "INVALID123XXX"

        every { headquartersRepository.findById(swiftCode) } returns Optional.empty()

        assertThrows<BankNotFoundException> {
            banksService.getBankDetails(swiftCode)
        }

        verify { headquartersRepository.findById(swiftCode) }
    }

    @Test
    fun `should return bank details by country ISO2`() {
        val countryISO2 = "US"
        val countryName = "United States"

        every { countryMapper.getCountryName(countryISO2) } returns countryName
        every { branchRepository.findAllByCountryISO2(countryISO2) } returns emptyList()
        every { headquartersRepository.findAllByCountryISO2(countryISO2) } returns emptyList()

        val result = banksService.getBankDetailsByCountry(countryISO2)

        assertEquals(countryISO2, result.countryISO2)
        assertEquals(countryName, result.countryName)
        assertEquals(emptyList(), result.swiftCodes)
    }

    @Test
    fun `should save branch details when headquarters exists`() {
        val headquartersSwiftCode = "ABCDEF12XXX"
        val branchSwiftCode = "ABCDEF12BRN"
        val bankDetailsDto = getBankDetailsDto(swiftCode = branchSwiftCode, isHeadquarter = false)

        val headquartersEntity = getHeadquartersEntity(headquartersSwiftCode)

        every { headquartersRepository.findById(headquartersSwiftCode) } returns Optional.of(headquartersEntity)
        every { branchRepository.existsById(branchSwiftCode) } returns false
        every { branchRepository.save(any()) } returns bankDetailsDto.toBranchEntity(headquartersEntity)

        val result = banksService.saveBankDetails(bankDetailsDto)

        assertEquals("Successfully saved bank information for SWIFT code $branchSwiftCode", result.message)
        verify { branchRepository.save(any()) }
    }


    @Test
    fun `should throw InvalidCountryCodeException when country name does not match`() {
        val bankDetails = getBankDetailsDto(swiftCode = "ABAABAABXXX", isHeadquarter = true, countryISO2 = "PL", countryName = "Czechia")

        every { countryMapper.getCountryName(bankDetails.countryISO2) } returns "Poland"

        assertThrows<InvalidCountryCodeException> {
            banksService.saveBankDetails(bankDetails)
        }
    }

    @Test
    fun `should throw HeadquartersMismatchException when SWIFT code format is incorrect`() {
        val bankDetails = getBankDetailsDto(swiftCode = "ABAABAABAAB", isHeadquarter = true)

        assertThrows<HeadquartersMismatchException> {
            banksService.saveBankDetails(bankDetails)
        }
    }

    @Test
    fun `should throw BankWithSwiftCodeExists when trying to save an existing bank`() {
        val bankDetails = getBankDetailsDto(swiftCode = "ABCDEF12XXX", isHeadquarter = true)

        every { headquartersRepository.existsById(bankDetails.swiftCode) } returns true

        assertThrows<BankWithSwiftCodeExists> {
            banksService.saveBankDetails(bankDetails)
        }
    }

    @Test
    fun `should delete branch when it exists`() {
        val swiftCode = "ABCDEF12BRN"

        every { branchRepository.existsById(swiftCode) } returns true
        justRun { branchRepository.deleteById(swiftCode) }

        val result = banksService.deleteBankDetails(swiftCode)

        assertEquals("Successfully deleted bank details for SWIFT code $swiftCode", result.message)
        verify { branchRepository.deleteById(swiftCode) }
    }


    @Test
    fun `should throw BankNotFoundException when trying to delete non-existing headquarters`() {
        val swiftCode = "ABCDEF12XXX"

        every { headquartersRepository.existsById(swiftCode) } returns false

        assertThrows<BankNotFoundException> {
            banksService.deleteBankDetails(swiftCode)
        }
    }

    @Test
    fun `should delete headquarters when it exists and has no branches`() {
        val swiftCode = "ABCDEF12XXX"
        val headquartersEntity = getHeadquartersEntity(swiftCode)

        every { headquartersRepository.existsById(swiftCode) } returns true
        every { headquartersRepository.findById(swiftCode) } returns Optional.of(headquartersEntity)
        every { headquartersRepository.deleteById(swiftCode) } returns Unit

        val result = banksService.deleteBankDetails(swiftCode)

        assertEquals("Successfully deleted bank details for SWIFT code $swiftCode", result.message)
        verify { headquartersRepository.deleteById(swiftCode) }
    }


    @Test
    fun `should throw CannotDeleteHeadquartersException when headquarters has branches`() {
        val swiftCode = "ABCDEF12XXX"
        val headquarters = mockk<HeadquartersEntity> {
            every { branches } returns mutableListOf(mockk<BranchEntity>())
        }

        every { headquartersRepository.existsById(swiftCode) } returns true
        every { headquartersRepository.findById(swiftCode) } returns Optional.of(headquarters)

        assertThrows<CannotDeleteHeadquartersException> {
            banksService.deleteBankDetails(swiftCode)
        }
    }

    @Test
    fun `should throw InvalidCountryCodeException when invalid ISO2 code is used`() {
        every { countryMapper.getCountryName("XX") } throws InvalidCountryCodeException("")
        assertThrows<InvalidCountryCodeException> {
            banksService.getBankDetailsByCountry("XX")
        }
    }
}

