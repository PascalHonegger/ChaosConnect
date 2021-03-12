package ch.chaosconnect.joestar

import ch.chaosconnect.rohan.EchoRequest
import ch.chaosconnect.rohan.EchoServiceGrpcKt
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val logger: Logger = LoggerFactory.getLogger(EchoJob::class.java)

@Singleton
class EchoJob(private val client: EchoServiceGrpcKt.EchoServiceCoroutineStub) {
    @Scheduled(fixedDelay = "10s")
    fun sayHelloJob() {
        runBlocking {
            logger.info("Sending hello")
            val response = client.echo(EchoRequest.newBuilder().setMessage("hello").build())
            logger.info("Received response: ${response.message}")
        }
    }
}