package com.remitly.swiftcodes.config

import com.remitly.swiftcodes.exception.InvalidCountryCodeException
import com.remitly.swiftcodes.getHeadquartersEntity
import com.remitly.swiftcodes.mapper.CountryMapper
import com.remitly.swiftcodes.model.entity.BranchEntity
import com.remitly.swiftcodes.model.entity.HeadquartersEntity
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DataInitializerTest {

    private val headquartersRepository = mockk<HeadquartersRepository>(relaxed = true)
    private val branchRepository = mockk<BranchRepository>(relaxed = true)
    private val countryMapper = mockk<CountryMapper>(relaxed = true)
    private val fileReader = mockk<FileReader>(relaxed = true)
    private val dataInitializer = DataInitializer(headquartersRepository, branchRepository, countryMapper, fileReader)

    @Test
    fun `should correctly map columns from CSV header`() {
        val headers = listOf("COUNTRY ISO2 CODE", "SWIFT CODE", "NAME", "COUNTRY NAME")
        val columnMap = dataInitializer.mapColumns(headers)

        assertEquals(4, columnMap.size)
        assertEquals(0, columnMap["COUNTRY ISO2 CODE"])
        assertEquals(1, columnMap["SWIFT CODE"])
        assertEquals(2, columnMap["NAME"])
        assertEquals(3, columnMap["COUNTRY NAME"])
    }

    @Test
    fun `should throw exception when required CSV columns are missing`() {
        val headers = listOf("SWIFT CODE", "NAME")

        val exception = assertThrows<IllegalArgumentException> {
            dataInitializer.mapColumns(headers)
        }

        assertEquals("CSV does not contain all required columns: [COUNTRY ISO2 CODE, SWIFT CODE, NAME, COUNTRY NAME], columns found: [SWIFT CODE, NAME]", exception.message)
    }

    @Test
    fun `should return null when parsing a record with invalid country name`() {
        val mockRecord = mockk<CSVRecord> {
            every { get(0) } returns "US"
            every { get(1) } returns "ABCDEF12XXX"
            every { get(2) } returns "Some Bank"
            every { get(3) } returns "United States"
            every { get(4) } returns "Some Address"
        }

        val columnMap = mapOf("COUNTRY ISO2 CODE" to 0, "SWIFT CODE" to 1, "NAME" to 2, "COUNTRY NAME" to 3, "ADDRESS" to 4)

        every { countryMapper.getCountryName("US") } returns "Wrong Country"

        val result = dataInitializer.parseRecord(mockRecord, columnMap)

        assertNull(result)
    }

    @Test
    fun `should return BankDetailsDto when record is valid`() {
        val mockRecord = mockk<CSVRecord> {
            every { get(0) } returns "US"
            every { get(1) } returns "ABCDEF12XXX"
            every { get(2) } returns "Some Bank"
            every { get(3) } returns "United States"
            every { get(4) } returns "Some Address"
        }

        val columnMap = mapOf("COUNTRY ISO2 CODE" to 0, "SWIFT CODE" to 1, "NAME" to 2, "COUNTRY NAME" to 3, "ADDRESS" to 4)

        every { countryMapper.getCountryName("US") } returns "United States"

        val result = dataInitializer.parseRecord(mockRecord, columnMap)

        assertNotNull(result)
        assertEquals("US", result.countryISO2)
        assertEquals("United States", result.countryName)
        assertEquals("ABCDEF12XXX", result.swiftCode)
        assertEquals("Some Bank", result.bankName)
        assertEquals("Some Address", result.address)
    }

    @Test
    fun `should return null when CSV parsing fails`() {
        val invalidRecord = mockk<CSVRecord> {
            every { get(any<Int>()) } throws IndexOutOfBoundsException()
        }

        val columnMap = mapOf("COUNTRY ISO2 CODE" to 0, "SWIFT CODE" to 1, "NAME" to 2, "COUNTRY NAME" to 3, "ADDRESS" to 4)

        val result = dataInitializer.parseRecord(invalidRecord, columnMap)

        assertNull(result)
    }

    @Test
    fun `should initialize database when repositories are empty`() {
        every { headquartersRepository.count() } returns 0L
        every { branchRepository.count() } returns 0L
        every { countryMapper.getCountryName("AL") } returns "ALBANIA"
        every { countryMapper.getCountryName("PL") } returns "POLAND"

        val csvData = """
            COUNTRY ISO2 CODE,SWIFT CODE,NAME,COUNTRY NAME
            AL,ABCDEF12XXX,Some Bank,Albania
            AL,ABCDEF12BRN,Some Bank,Albania
            PL,ABCDEF24BRN,Pekao,Poland
        """.trimIndent()

        val savedHeadquartersEntity = getHeadquartersEntity(swiftCode = "ABCDEF12XXX", bankName = "Some Bank", countryISO2 = "AL", address = null, countryName = "Albania")
        every { headquartersRepository.findById(savedHeadquartersEntity.swiftCode) } returns Optional.of(savedHeadquartersEntity)

        val inputStream = csvData.byteInputStream()
        every { fileReader.getReader(CSV_FILE_NAME) } returns BufferedReader(InputStreamReader(inputStream))

        runBlocking {
            dataInitializer.initDatabase().run(null)
        }

        verify { headquartersRepository.saveAll(any<List<HeadquartersEntity>>()) }
        verify { branchRepository.saveAll(any<List<BranchEntity>>()) }
    }

    @Test
    fun `should not save any failed records when initialize Database`() {
        every { headquartersRepository.count() } returns 0L
        every { branchRepository.count() } returns 0L
        every { countryMapper.getCountryName("AL") } returns "ALBANIA"
        every { countryMapper.getCountryName("PL") } returns "POLAND"
        every { countryMapper.getCountryName("XX") } throws InvalidCountryCodeException("")

        val csvData = """
            COUNTRY ISO2 CODE,SWIFT CODE,NAME,COUNTRY NAME
            AL,ABCDEF13BRN,Some Bank,Albania
            AL,ABCDEF12BRN,Some Bank,Albania
            PL,ABCDEF24BRN,Pekao,Poland
            PL,ABCDEF24BR,Pekao,Poland
            XX,ABCDEF24BR,Pekao,Poland
        """.trimIndent()

        val inputStream = csvData.byteInputStream()
        every { fileReader.getReader(CSV_FILE_NAME) } returns BufferedReader(InputStreamReader(inputStream))

        runBlocking {
            dataInitializer.initDatabase().run(null)
        }

        verify { headquartersRepository.saveAll(emptyList()) }
        verify { branchRepository.saveAll(emptyList()) }
    }

    @Test
    fun `should not initialize database when repositories are not empty`() {
        every { headquartersRepository.count() } returns 5L
        every { branchRepository.count() } returns 3L

        runBlocking {
            dataInitializer.initDatabase().run(null)
        }

        verify(exactly = 0) { headquartersRepository.saveAll(any<List<HeadquartersEntity>>()) }
        verify(exactly = 0) { branchRepository.saveAll(any<List<BranchEntity>>()) }
    }
}
