package ch.chaosconnect.rohan;

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.Coordinate
import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.GameUpdateResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

@Singleton
class GameEndpoint(private val service: GameService) :
    GameServiceGrpcKt.GameServiceCoroutineImplBase() {

    override suspend fun placePiece(request: Coordinate): Empty {
        service.placePiece(request.row, request.column)
        return Empty.getDefaultInstance()
    }

    override fun getGameUpdates(request: Empty): Flow<GameUpdateResponse> =
        service.getGameUpdates()
            .map {
                GameUpdateResponse
                    .newBuilder()
                    .setEvent(it.first)
                    .setNewState(it.second)
                    .build()
            }
}