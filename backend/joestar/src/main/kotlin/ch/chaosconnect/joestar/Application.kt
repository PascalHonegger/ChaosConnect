package ch.chaosconnect.joestar

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("ch.chaosconnect.joestar")
		.start()
}

