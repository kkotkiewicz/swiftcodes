package com.remitly.swiftcodes.integrationTests

import com.remitly.swiftcodes.model.entity.BranchEntity
import com.remitly.swiftcodes.model.entity.HeadquartersEntity
import com.remitly.swiftcodes.repository.BranchRepository
import com.remitly.swiftcodes.repository.HeadquartersRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SwiftCodesIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val headquartersRepository: HeadquartersRepository,
    private val branchRepository: BranchRepository
) {

    @BeforeEach
    fun setup() {
        headquartersRepository.deleteAll()
        branchRepository.deleteAll()

        val headquarter = HeadquartersEntity(
            swiftCode = "HQ123456XXX",
            bankName = "Test Bank HQ",
            countryISO2 = "US",
            countryName = "United States",
            address = "123 Wall Street",
            branches = mutableListOf()
        )

        val branch1 = BranchEntity(
            swiftCode = "HQ123456BR1",
            bankName = "Test Bank Branch",
            countryISO2 = "US",
            countryName = "United States",
            address = "456 Main Street",
            headquarters = headquarter
        )

        val branch2 = BranchEntity(
            swiftCode = "HQ123456BR2",
            bankName = "Test Bank Branch 2",
            countryISO2 = "US",
            countryName = "United States",
            address = "789 Broadway",
            headquarters = headquarter
        )

        headquarter.branches.addAll(listOf(branch1, branch2))

        headquartersRepository.save(headquarter)
        branchRepository.saveAll(listOf(branch1, branch2))
    }

    @Test
    fun `should retrieve details of a headquarter swift code with branches`() {
        mockMvc.get("/v1/swift-codes/HQ123456XXX")
            .andExpect {
                status { isOk() }
                jsonPath("$.bankName") { value("Test Bank HQ") }
                jsonPath("$.isHeadquarter") { value(true) }
                jsonPath("$.branches.length()") { value(2) }
            }
    }

    @Test
    fun `should retrieve details of a branch swift code`() {
        mockMvc.get("/v1/swift-codes/HQ123456BR1")
            .andExpect {
                status { isOk() }
                jsonPath("$.bankName") { value("Test Bank Branch") }
                jsonPath("$.isHeadquarter") { value(false) }
                jsonPath("$.swiftCode") { value("HQ123456BR1") }
            }
    }

    @Test
    fun `should return all SWIFT codes for a country`() {
        mockMvc.get("/v1/swift-codes/country/US")
            .andExpect {
                status { isOk() }
                jsonPath("$.countryISO2") { value("US") }
                jsonPath("$.swiftCodes.length()") { value(3) } // 1 HQ + 2 Branches
            }
    }

    @Test
    fun `should add a new SWIFT code entry`() {
        val request = """
            {
                "address": "999 New Street",
                "bankName": "New Test Bank",
                "countryISO2": "CA",
                "countryName": "Canada",
                "isHeadquarter": true,
                "swiftCode": "HQ987654XXX"
            }
        """

        mockMvc.post("/v1/swift-codes") {
            contentType = MediaType.APPLICATION_JSON
            content = request
        }.andExpect {
            status { isOk() }
            jsonPath("$.message") { value("Successfully saved bank information for SWIFT code HQ987654XXX") }
        }

        assertTrue(headquartersRepository.existsById("HQ987654XXX"))
    }

    @Test
    fun `should not add a new branch SWIFT code entry if HQ does not exist`() {
        val request = """
            {
                "address": "998 New Street",
                "bankName": "New Test Bank",
                "countryISO2": "CA",
                "countryName": "Canada",
                "isHeadquarter": false,
                "swiftCode": "BR987654BR1"
            }
        """

        mockMvc.post("/v1/swift-codes") {
            contentType = MediaType.APPLICATION_JSON
            content = request
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.message") { value("Headquarters not found for branch SWIFT code: BR987654BR1") }
        }

        assertFalse(headquartersRepository.existsById("BR987654BR1"))
    }

    @Test
    fun `should delete a SWIFT code`() {
        mockMvc.delete("/v1/swift-codes/HQ123456BR1")
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("Successfully deleted bank details for SWIFT code HQ123456BR1") }
            }

        assertFalse(headquartersRepository.existsById("HQ123456BR1"))
    }

    @Test
    fun `should not delete a headquarter SWIFT code if branches exist`() {
        mockMvc.delete("/v1/swift-codes/HQ123456XXX")
            .andExpect {
                status { isConflict() }
            }

        assertTrue(headquartersRepository.existsById("HQ123456XXX"))
    }
}
