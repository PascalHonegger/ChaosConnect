package ch.chaosconnect.joestar

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.GameUpdateResponse
import ch.chaosconnect.rohan.EchoRequest
import ch.chaosconnect.rohan.EchoServiceGrpcKt
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
@Requires(notEnv = ["test"], missingProperty = "mocks.rohan")
class RohanServiceImpl(
    private val echoService: EchoServiceGrpcKt.EchoServiceCoroutineStub,
    private val gameService: GameServiceGrpcKt.GameServiceCoroutineStub
) : RohanService {
    override suspend fun echo(message: String): String {
        val payload = EchoRequest
            .newBuilder()
            .setMessage(message)
            .build()
        val response = echoService.echo(payload)
        return response.message
    }

    override fun getGameUpdates(): Flow<GameUpdateResponse> {
        return gameService.getGameUpdates(Empty.getDefaultInstance())
    }
}