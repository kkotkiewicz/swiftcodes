package com.remitly.swiftcodes.config

const val COUNTRY_ISO2 = "COUNTRY ISO2 CODE"
const val SWIFT_CODE = "SWIFT_CODE"
const val COUNTRY_NAME = "COUNTRY NAME"
const val BANK_NAME = "NAME"
const val ADDRESS = "ADDRESS"

const val CSV_FILE_NAME = "swift_codes.csv"

const val HEADQUARTERS_SUFFIX = "XXX"
val HEADQUARTERS_REGEX = Regex("^.{8,}$HEADQUARTERS_SUFFIX$")
