package ch.chaosconnect.joestar

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val logger: Logger = LoggerFactory.getLogger(EchoJob::class.java)

@Singleton
@Requires(notEnv = ["test"])
class EchoJob(private val client: RohanService) {
    @Scheduled(fixedDelay = "10s")
    fun sayHelloJob() {
        runBlocking {
            logger.info("Sending hello")
            val response = client.echo("hello")
            logger.info("Received response: $response")
        }
    }
}