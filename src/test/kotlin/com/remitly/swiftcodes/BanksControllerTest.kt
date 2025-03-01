package com.remitly.swiftcodes

import com.fasterxml.jackson.databind.ObjectMapper
import com.remitly.swiftcodes.mapper.CountryMapper
import com.remitly.swiftcodes.model.response.CountryBankDetails
import com.remitly.swiftcodes.model.response.Message
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.Optional

@ExtendWith(SpringExtension::class)
class SwiftCodesControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var banksService: BanksService
    private lateinit var headquartersRepository: HeadquartersRepository
    private lateinit var branchRepository: BranchRepository
    private lateinit var countryMapper: CountryMapper
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        headquartersRepository = mockk(relaxed = true)
        branchRepository = mockk(relaxed = true)
        countryMapper = CountryMapper()
        objectMapper = ObjectMapper()

        banksService = BanksService(headquartersRepository, branchRepository, countryMapper)
        val controller = SwiftCodesController(banksService)

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `should return bank details when SWIFT code exists`() {
        val swiftCode = "ABCDEF12XXX"
        val headquartersEntity = getHeadquartersEntity(swiftCode = swiftCode, bankName = "Some Bank")

        every { headquartersRepository.findById(swiftCode) } returns Optional.of(headquartersEntity)

        mockMvc.get("/v1/swift-codes/$swiftCode")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.swiftCode") { value(swiftCode) }
                jsonPath("$.bankName") { value("Some Bank") }
            }
    }

    @Test
    fun `should return 404 when SWIFT code does not exist`() {
        val swiftCode = "INVALID1XXX"

        every { headquartersRepository.findById(swiftCode) } returns Optional.empty()

        mockMvc.get("/v1/swift-codes/$swiftCode")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return bank details by country ISO2`() {
        val countryISO2 = "PL"
        val countryName = "Poland"
        val swiftCodes = mutableListOf(getHeadquartersEntity(swiftCode = "AAAAAAAAXXX", countryName = "Poland", countryISO2 = "PL"))
        val response = CountryBankDetails(countryISO2, countryName, swiftCodes.map { it.toBankBranchDetails() })

        every { branchRepository.findAllByCountryISO2(countryISO2) } returns emptyList()
        every { headquartersRepository.findAllByCountryISO2(countryISO2) } returns swiftCodes

        mockMvc.get("/v1/swift-codes/country/$countryISO2")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.countryISO2") { value(countryISO2) }
                jsonPath("$.countryName") { value(countryName) }
                jsonPath("$.swiftCodes[*].swiftCode") { value(response.swiftCodes[0].swiftCode) }
            }
    }

    @Test
    fun `should return 400 for invalid SWIFT code format`() {
        val invalidSwiftCode = "INVALID"
        mockMvc.get("/v1/swift-codes/$invalidSwiftCode")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should delete bank details`() {
        val swiftCode = "ABCDEF12XXX"
        val headquartersEntity = getHeadquartersEntity(swiftCode = swiftCode, branches = mutableListOf())
        every { headquartersRepository.existsById(swiftCode) } returns true
        every { headquartersRepository.deleteById(swiftCode) } returns Unit
        every { headquartersRepository.findById(swiftCode) } returns Optional.of(headquartersEntity)

        mockMvc.delete("/v1/swift-codes/$swiftCode")
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("Successfully deleted bank details for SWIFT code $swiftCode") }
            }
    }

    @Test
    fun `should return 404 when deleting non-existing bank`() {
        val swiftCode = "INVALID1XXX"

        every { headquartersRepository.existsById(swiftCode) } returns false

        mockMvc.delete("/v1/swift-codes/$swiftCode")
            .andExpect {
                status { isNotFound() }
            }
    }
}
