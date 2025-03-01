package com.remitly.swiftcodes.config

import com.remitly.swiftcodes.exception.InvalidCountryCodeException
import java.util.Locale

object CountryMapping {
    private const val ENGLISH = "en"

    fun getCountryName(iso2: String): String {
        return Locale.of(ENGLISH, iso2.uppercase()).displayCountry.takeIf { it.isNotEmpty() }
            ?: throw InvalidCountryCodeException("Invalid country ISO2 code: $iso2")
    }
}

