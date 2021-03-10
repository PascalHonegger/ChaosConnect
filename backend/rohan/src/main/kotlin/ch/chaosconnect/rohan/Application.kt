package ch.chaosconnect.rohan

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("ch.chaosconnect.rohan")
		.start()
}

