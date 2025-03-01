package com.remitly.swiftcodes.config

import com.remitly.swiftcodes.mapper.CountryMapper
import com.remitly.swiftcodes.model.dto.BankDetailsDto
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

@Configuration
class DataInitializer(
    private val headquartersRepository: HeadquartersRepository,
    private val branchRepository: BranchRepository,
    private val countryMapper: CountryMapper,
    private val fileReader: FileReader
) {
    @Bean
    fun initDatabase(): ApplicationRunner {
        return ApplicationRunner {
            if (headquartersRepository.count() == 0L && branchRepository.count() == 0L) {
                val banks = parseCsv(CSV_FILE_NAME) ?: return@ApplicationRunner
                val headquarters = banks.filter { it.isHeadquarter }
                val branches = banks.filterNot { it.isHeadquarter }
                headquartersRepository.saveAll(headquarters.map { it.toHeadquartersEntity(mutableListOf()) })
                val branchEntities = branches.mapNotNull { branch ->
                    val headquartersForBranch = headquartersRepository.findById(
                        branch.swiftCode.replaceRange(branch.swiftCode.length - 3, branch.swiftCode.length, HEADQUARTERS_SUFFIX)
                    )
                    if (headquartersForBranch.isPresent) {
                        branch.toBranchEntity(headquartersForBranch.get())
                    } else null
                }

                branchRepository.saveAll(branchEntities)

                println("Database initialized with ${headquartersRepository.count()} headquarters and ${branchRepository.count()} branches.")
            }
        }
    }

     fun parseCsv(filename: String): List<BankDetailsDto>? {
        val reader = fileReader.getReader(filename)

        CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).setIgnoreSurroundingSpaces(true).build()).use { csvParser ->
            val columnMap: Map<String, Int>
            try {
                columnMap = mapColumns(csvParser.headerNames)
            } catch (ex: IllegalArgumentException) {
                println(ex.message)
                return null
            }
            return csvParser.records.mapNotNull { parseRecord(it, columnMap) }
        }
    }

     fun mapColumns(headers: List<String>): Map<String, Int> {
        val expectedColumns = setOf(COUNTRY_ISO2, SWIFT_CODE, BANK_NAME, COUNTRY_NAME)

        val columnMap = headers.withIndex().associate { (index, name) -> name.uppercase() to index }

        if (!expectedColumns.all { it in columnMap }) {
            throw IllegalArgumentException("CSV does not contain all required columns: $expectedColumns, columns found: $headers")
        }

        return columnMap
    }

     fun parseRecord(record: CSVRecord, columnMap: Map<String, Int>): BankDetailsDto? {
        try {
            val swiftCode = record.get(columnMap[SWIFT_CODE]!!)
            val countryISO2 = record.get(columnMap[COUNTRY_ISO2]!!)
            val countryName = record.get(columnMap[COUNTRY_NAME]!!)
            val bankName = record.get(columnMap[BANK_NAME]!!)
            val address = columnMap[ADDRESS]?.let { record.get(it).takeIf { it.isNotBlank() } }

            if (!countryMapper.getCountryName(countryISO2).equals(countryName, ignoreCase = true)) {
                return null
            }

            return BankDetailsDto(
                bankName = bankName,
                countryISO2 = countryISO2,
                countryName = countryName,
                swiftCode = swiftCode,
                address = address,
                isHeadquarter = swiftCode.endsWith(HEADQUARTERS_SUFFIX)
            )
        } catch (ex: Exception) {
            return null
        }
    }
}

@Component
class FileReader {
    fun getReader(filename: String): BufferedReader {
        val inputStream = ClassPathResource(filename).inputStream
        return BufferedReader(InputStreamReader(inputStream))
    }
}
