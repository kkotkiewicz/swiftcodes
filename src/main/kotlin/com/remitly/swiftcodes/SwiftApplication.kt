package com.remitly.swiftcodes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SwiftApplication

fun main(args: Array<String>) {
	runApplication<SwiftApplication>(*args)
}
