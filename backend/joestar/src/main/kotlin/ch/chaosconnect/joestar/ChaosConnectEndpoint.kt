package ch.chaosconnect.joestar

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.Coordinate
import ch.chaosconnect.api.game.GameUpdateEvent
import ch.chaosconnect.api.joestar.ChaosConnectServiceGrpcKt
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
class ChaosConnectEndpoint(private val gameStateService: GameStateService) :
    ChaosConnectServiceGrpcKt.ChaosConnectServiceCoroutineImplBase() {
    override fun getGameUpdates(request: Empty): Flow<GameUpdateEvent> =
        gameStateService.getStateAndUpdates()

    override suspend fun placePiece(request: Coordinate): Empty =
        Empty.getDefaultInstance()
}