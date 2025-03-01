package com.remitly.swiftcodes.mapper

import com.remitly.swiftcodes.exception.InvalidCountryCodeException
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class CountryMapper {
    private val countryCodes = Locale.getISOCountries().toSet()

    fun getCountryName(iso2: String): String {
        val uppercaseIso2 = iso2.uppercase()
        if (!countryCodes.contains(uppercaseIso2)) {
            throw InvalidCountryCodeException("Invalid country ISO2 code: $iso2")
        }
        return Locale.of("", uppercaseIso2).getDisplayCountry(Locale.ENGLISH)
    }
}

