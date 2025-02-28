package com.remitly.swiftcodes

import com.remitly.swiftcodes.config.CountryMapping
import com.remitly.swiftcodes.exception.*
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BanksService(
    private val headquartersRepository: HeadquartersRepository,
    private val branchRepository: BranchRepository,
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
        val countryName = CountryMapping.getCountryName(countryISO2)

        val swiftCodes = branchRepository.findAllByCountryISO2(countryISO2).map { it.toBankBranchDetails() } +
                headquartersRepository.findAllByCountryISO2(countryISO2).map { it.toBankBranchDetails() }

        return CountryBankDetails(countryISO2 = countryISO2, countryName = countryName, swiftCodes = swiftCodes)
    }

    @Transactional
    fun saveBankDetails(bankDetails: BankDetailsDto): Message {
        val countryName = CountryMapping.getCountryName(bankDetails.countryISO2)
        if (!bankDetails.countryName.contentEquals(countryName, ignoreCase = true)) {
            throw InvalidCountryCodeException("Country name: ${bankDetails.countryName} does not match expected country name: $countryName")
        }

        val isHeadquarter = isHeadquarters(bankDetails.swiftCode)
        if (isHeadquarter != bankDetails.isHeadquarter) {
            throw HeadquartersMismatchException(
                if (bankDetails.isHeadquarter) {
                    "Headquarters SWIFT code must be 11 chars long and end with 'XXX'"
                } else {
                    "Branch SWIFT code must be 11 chars long and must not end with 'XXX'"
                }
            )
        }

        if (!bankDetails.isHeadquarter) {
            val headquarters = headquartersRepository.findById(getHeadquartersSwiftCode(bankDetails.swiftCode))
                .orElseThrow { BankNotFoundException("Headquarters not found for branch SWIFT code: ${bankDetails.swiftCode}") }

            headquarters.branches + bankDetails.toBranchBankDetails()
            headquartersRepository.save(headquarters)
            branchRepository.save(bankDetails.toBranchEntity(headquarters.swiftCode))
        } else {
            headquartersRepository.save(bankDetails.toHeadquartersEntity(mutableListOf()))
        }

        return Message("Successfully saved bank information for SWIFT code ${bankDetails.swiftCode}")
    }

    fun deleteBankDetails(swiftCode: String): Message {
        if (isHeadquarters(swiftCode)) {
            if (branchRepository.findAllByHeadquartersId(swiftCode).isEmpty()) {
                headquartersRepository.deleteById(swiftCode)
            } else {
                throw CannotDeleteHeadquartersException("Cannot delete headquarters because it has branches. Delete all branches first.")
            }
        } else {
            branchRepository.deleteById(swiftCode)
        }
        return Message("Successfully deleted bank details for SWIFT code $swiftCode")
    }

    private fun isHeadquarters(swiftCode: String): Boolean = Regex("^.{8,}XXX\$").matches(swiftCode)

    private fun getHeadquartersSwiftCode(swiftCode: String): String {
        if (swiftCode.length >= 3)
            return swiftCode.replaceRange(swiftCode.length - 3, swiftCode.length, "XXX")
        else throw InvalidSwiftCodeException("Invalid SWIFT code: $swiftCode")
    }
}

data class BankDetails(
    val address: String?,
    val bankName: String,
    val countryISO2: String,
    val countryName: String,
    val isHeadquarter: Boolean,
    val swiftCode: String,
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
