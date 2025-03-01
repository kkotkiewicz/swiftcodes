package com.remitly.swiftcodes.model.response

import com.fasterxml.jackson.annotation.JsonInclude

data class ErrorResponse(
    val message: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val errors: Map<String, String?>? = null
)