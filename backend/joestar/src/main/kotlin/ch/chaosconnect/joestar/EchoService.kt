package ch.chaosconnect.joestar

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val logger: Logger = LoggerFactory.getLogger(EchoService::class.java)

@Singleton
class EchoService(private val rohanClient: RohanService) {
    fun echo(message: String): String {
        logger.info("Got message $message")
        return "Joestar said $message"
    }

    suspend fun echoViaRohan(message: String): String {
        logger.info("Got message $message, redirecting to Rohan")
        return rohanClient.echo(message)
    }
}