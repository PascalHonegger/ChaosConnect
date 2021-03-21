package ch.chaosconnect.joestar

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class JoestarTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    lateinit var echoStub: EchoServiceGrpcKt.EchoServiceCoroutineStub

    @get:MockBean(RohanService::class)
    val rohanService = mockk<RohanService>()

    @get:MockBean(GameStateService::class)
    val gameStateService = mockk<GameStateService>()

    @Test
    fun `service should start`() {
        assertTrue(application.isRunning)
    }

    @Test
    fun `echo should return message from rohan service`() {
        runBlocking {
            val request = EchoRequest.newBuilder().setMessage("test").build()
            coEvery { rohanService.echo("test") } returns "expected"
            val response = echoStub.echo(request)
            assertEquals(response.message, "expected")
        }
    }
}

@Factory
internal class Clients {
    @Bean
    fun joestarEchoStub(
        @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
    ): EchoServiceGrpcKt.EchoServiceCoroutineStub {
        return EchoServiceGrpcKt.EchoServiceCoroutineStub(channel)
    }
}