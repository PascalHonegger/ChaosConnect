package ch.chaosconnect.joestar

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory

import io.micronaut.grpc.server.GrpcServerChannel

import io.micronaut.grpc.annotation.GrpcChannel
import kotlinx.coroutines.runBlocking


@MicronautTest
class JoestarTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    lateinit var echoStub: EchoServiceGrpcKt.EchoServiceCoroutineStub

    @Test
    fun `service should start`() {
        Assertions.assertTrue(application.isRunning)
    }

    @Test
    fun `echo should return sent message`() {
        runBlocking {
            val request = EchoRequest.newBuilder().setMessage("test").build()
            val response = echoStub.echo(request)
            Assertions.assertEquals(response.message, "Joestar said test")
        }
    }
}

@Factory
internal class Clients {
    @Bean
    fun blockingStub(
        @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
    ): EchoServiceGrpcKt.EchoServiceCoroutineStub {
        return EchoServiceGrpcKt.EchoServiceCoroutineStub(channel)
    }
}