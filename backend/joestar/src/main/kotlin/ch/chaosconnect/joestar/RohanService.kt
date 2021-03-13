package ch.chaosconnect.joestar

import ch.chaosconnect.rohan.EchoRequest
import ch.chaosconnect.rohan.EchoServiceGrpcKt
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

interface RohanService {
    suspend fun echo(message: String): String
}

@Singleton
@Requires(notEnv = ["test"])
class RohanServiceIml(private val echoService: EchoServiceGrpcKt.EchoServiceCoroutineStub) : RohanService {
    override suspend fun echo(message: String): String {
        val payload = EchoRequest
            .newBuilder()
            .setMessage(message)
            .build()
        val response = echoService.echo(payload)
        return response.message
    }
}