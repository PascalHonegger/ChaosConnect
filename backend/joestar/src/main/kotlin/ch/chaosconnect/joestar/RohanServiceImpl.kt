package ch.chaosconnect.joestar

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.GameUpdateResponse
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
@Requires(notEnv = ["test"], missingProperty = "mocks.rohan")
class RohanServiceImpl(private val gameService: GameServiceGrpcKt.GameServiceCoroutineStub) :
    RohanService {
    override fun getGameUpdates(): Flow<GameUpdateResponse> {
        return gameService.getGameUpdates(Empty.getDefaultInstance())
    }
}