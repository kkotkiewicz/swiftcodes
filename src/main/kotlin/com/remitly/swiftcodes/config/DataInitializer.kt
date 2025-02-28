package com.remitly.swiftcodes.config

import com.remitly.swiftcodes.BankDetailsDto
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.BufferedReader
import java.io.InputStreamReader

@Configuration
class DataInitializer(
    private val headquartersRepository: HeadquartersRepository,
    private val branchRepository: BranchRepository
) {
    @Bean
    fun initDatabase(): ApplicationRunner {
        return ApplicationRunner {
            if (headquartersRepository.count() == 0L && branchRepository.count() == 0L) {
                val banks = parseCsv("swift_codes.csv")
                val headquarters = banks.filter { it.isHeadquarter }
                val branches = banks.filterNot { it.isHeadquarter }

                headquartersRepository.saveAll(headquarters.map { it.toHeadquartersEntity(mutableListOf()) })
                branchRepository.saveAll(branches.map {
                    it.toBranchEntity(it.swiftCode.replaceRange(
                        it.swiftCode.length - 3,
                        it.swiftCode.length,
                        "XXX"
                    ))
                })

                println("Database initialized with ${headquarters.size} headquarters and ${branches.size} branches.")
            }
        }
    }

    private fun parseCsv(filename: String): List<BankDetailsDto> {
        val inputStream = ClassPathResource(filename).inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))

        val headers = reader.readLine().split("\t").map { it.trim() }
        val columnMap = mapColumns(headers)

        return reader.lineSequence()
            .mapNotNull { line -> parseLine(line, columnMap) }
            .toList()
    }

    private fun mapColumns(headers: List<String>): Map<String, Int> {
        val expectedColumns = setOf(
            "COUNTRY ISO2 CODE", "SWIFT CODE", "NAME", "ADDRESS", "COUNTRY NAME"
        )
        val columnMap = headers.withIndex().associate { (index, name) -> name.uppercase() to index }

        if (!expectedColumns.all { it in columnMap }) {
            throw IllegalArgumentException("CSV does not contain all required columns: $expectedColumns")
        }

        return columnMap
    }

    private fun parseLine(line: String, columnMap: Map<String, Int>): BankDetailsDto? {
        val columns = line.split("\t").map { it.trim() }

        val swiftCode = columns[columnMap["SWIFT CODE"]!!]
        return BankDetailsDto(
            bankName = columns[columnMap["NAME"]!!],
            countryISO2 = columns[columnMap["COUNTRY ISO2 CODE"]!!],
            countryName = columns[columnMap["COUNTRY NAME"]!!],
            swiftCode = swiftCode,
            address = columnMap["ADDRESS"]?.let { columns[it].takeIf { it.isNotBlank() } },
            isHeadquarter = swiftCode.endsWith("XXX")
        )
    }
}
