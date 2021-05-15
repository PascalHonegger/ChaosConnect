package ch.chaosconnect.rohan.endpoints

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.PlacePieceRequest
import ch.chaosconnect.api.game.StartPlayingRequest
import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.GameUpdateResponse
import ch.chaosconnect.rohan.services.GameService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

@Singleton
class GameEndpoint(private val service: GameService) :
    GameServiceGrpcKt.GameServiceCoroutineImplBase() {

    override suspend fun startPlaying(request: StartPlayingRequest): Empty {
        service.startPlaying(faction = request.faction)
        return Empty.getDefaultInstance()
    }

    override suspend fun stopPlaying(request: Empty): Empty {
        service.stopPlaying()
        return Empty.getDefaultInstance()
    }

    override suspend fun placePiece(request: PlacePieceRequest): Empty {
        service.placePiece(columnIndex = request.column)
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
