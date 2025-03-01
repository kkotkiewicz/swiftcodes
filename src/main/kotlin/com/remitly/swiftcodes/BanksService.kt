package com.remitly.swiftcodes

import com.remitly.swiftcodes.mapper.CountryMapper
import com.remitly.swiftcodes.config.HEADQUARTERS_SUFFIX
import com.remitly.swiftcodes.exception.BankNotFoundException
import com.remitly.swiftcodes.exception.BankWithSwiftCodeExists
import com.remitly.swiftcodes.exception.HeadquartersMismatchException
import com.remitly.swiftcodes.exception.InvalidCountryCodeException
import com.remitly.swiftcodes.exception.CannotDeleteHeadquartersException
import com.remitly.swiftcodes.exception.InvalidSwiftCodeException
import com.remitly.swiftcodes.model.response.BankDetails
import com.remitly.swiftcodes.model.response.CountryBankDetails
import com.remitly.swiftcodes.model.response.Message
import com.remitly.swiftcodes.model.dto.BankDetailsDto
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BanksService(
    private val headquartersRepository: HeadquartersRepository,
    private val branchRepository: BranchRepository,
    private val countryMapper: CountryMapper
) {
    fun getBankDetails(swiftCode: String): BankDetails {
        if (isHeadquarters(swiftCode)) {
            return headquartersRepository.findById(swiftCode)
                .orElseThrow { BankNotFoundException("Bank not found for SWIFT code: $swiftCode") }
                .toBankDetails()
        }
        return branchRepository.findById(swiftCode)
            .orElseThrow { BankNotFoundException("Bank not found for SWIFT code: $swiftCode") }
            .toBankDetails()
    }

    fun getBankDetailsByCountry(countryISO2: String): CountryBankDetails {
        val countryName = countryMapper.getCountryName(countryISO2)

        val swiftCodes = branchRepository.findAllByCountryISO2(countryISO2).map { it.toBankBranchDetails() } +
                headquartersRepository.findAllByCountryISO2(countryISO2).map { it.toBankBranchDetails() }

        return CountryBankDetails(countryISO2 = countryISO2, countryName = countryName, swiftCodes = swiftCodes)
    }

    @Transactional
    fun saveBankDetails(bankDetails: BankDetailsDto): Message {
        validateCountryName(bankDetails)
        validateSwiftCode(bankDetails)
        checkExistence(bankDetails)

        if (!bankDetails.isHeadquarter) {
            saveBranch(bankDetails)
        } else {
            saveHeadquarters(bankDetails)
        }

        return Message("Successfully saved bank information for SWIFT code ${bankDetails.swiftCode}")
    }

    fun deleteBankDetails(swiftCode: String): Message {
        if (isHeadquarters(swiftCode)) {
            deleteHeadquarters(swiftCode)
        } else {
            deleteBranch(swiftCode)
        }
        return Message("Successfully deleted bank details for SWIFT code $swiftCode")
    }

    private fun validateCountryName(bankDetails: BankDetailsDto) {
        val expectedCountryName = countryMapper.getCountryName(bankDetails.countryISO2)
        if (!bankDetails.countryName.equals(expectedCountryName, ignoreCase = true)) {
            throw InvalidCountryCodeException("Country name: ${bankDetails.countryName} does not match expected country name: $expectedCountryName")
        }
    }

    private fun validateSwiftCode(bankDetails: BankDetailsDto) {
        val isHeadquarter = isHeadquarters(bankDetails.swiftCode)
        if (isHeadquarter != bankDetails.isHeadquarter) {
            throw HeadquartersMismatchException(
                if (bankDetails.isHeadquarter) {
                    "Headquarters SWIFT code must be 11 chars long and end with '$HEADQUARTERS_SUFFIX'"
                } else {
                    "Branch SWIFT code must be 11 chars long and must not end with '$HEADQUARTERS_SUFFIX'"
                }
            )
        }
    }

    private fun checkExistence(bankDetails: BankDetailsDto) {
        val existsInRepo = if (bankDetails.isHeadquarter) {
            headquartersRepository.existsById(bankDetails.swiftCode)
        } else {
            branchRepository.existsById(bankDetails.swiftCode)
        }

        if (existsInRepo) {
            throw BankWithSwiftCodeExists("Bank with SWIFT code: ${bankDetails.swiftCode} already exists")
        }
    }

    private fun saveHeadquarters(bankDetails: BankDetailsDto): Message {
        headquartersRepository.save(bankDetails.toHeadquartersEntity(mutableListOf()))
        return Message("Successfully saved headquarters for SWIFT code ${bankDetails.swiftCode}")
    }

    private fun saveBranch(bankDetails: BankDetailsDto): Message {
        val headquartersSwiftCode = getHeadquartersSwiftCode(bankDetails.swiftCode)
        val headquarters = headquartersRepository.findById(headquartersSwiftCode)
            .orElseThrow { BankNotFoundException("Headquarters not found for branch SWIFT code: ${bankDetails.swiftCode}") }

        branchRepository.save(bankDetails.toBranchEntity(headquarters))
        return Message("Successfully saved branch for SWIFT code ${bankDetails.swiftCode}")
    }

    private fun deleteHeadquarters(swiftCode: String) {
        if (!headquartersRepository.existsById(swiftCode)) {
            throw BankNotFoundException("Headquarters not found with SWIFT code: $swiftCode")
        }

        if (headquartersRepository.findById(swiftCode).get().branches.isNotEmpty()) {
            throw CannotDeleteHeadquartersException("Cannot delete headquarters because it has branches. Delete all branches first.")
        }

        headquartersRepository.deleteById(swiftCode)
    }

    private fun deleteBranch(swiftCode: String) {
        if (!branchRepository.existsById(swiftCode)) {
            throw BankNotFoundException("Branch not found with SWIFT code: $swiftCode")
        }
        branchRepository.deleteById(swiftCode)
    }

    private fun isHeadquarters(swiftCode: String): Boolean {
        return swiftCode.matches(Regex("^.{8,}$HEADQUARTERS_SUFFIX$"))
    }

    private fun getHeadquartersSwiftCode(swiftCode: String): String {
        if (swiftCode.length >= 3) {
            return swiftCode.replaceRange(swiftCode.length - 3, swiftCode.length, HEADQUARTERS_SUFFIX)
        } else {
            throw InvalidSwiftCodeException("Invalid SWIFT code: $swiftCode")
        }
    }
}
