package ch.chaosconnect.joestar

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.Coordinate
import ch.chaosconnect.api.game.GameUpdateEvent
import ch.chaosconnect.api.game.PieceChanged
import ch.chaosconnect.api.game.PieceState
import ch.chaosconnect.api.joestar.ChaosConnectServiceGrpcKt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val logger: Logger =
    LoggerFactory.getLogger(ChaosConnectEndpoint::class.java)

@Singleton
class ChaosConnectEndpoint :
    ChaosConnectServiceGrpcKt.ChaosConnectServiceCoroutineImplBase() {
    override fun getGameUpdates(request: Empty): Flow<GameUpdateEvent> =
        flow {
            logger.info("ChaosConnectEndpoint called")
            while (true) {
                logger.info("before delay")
                delay(1000)
                logger.info("after delay")
                val pieceState = PieceState.newBuilder()
                    .setOwner("0wner")
                    .setPosition(
                        Coordinate.newBuilder()
                            .setRow(0)
                            .setColumn(0)
                            .build()
                    )
                val pieceChanged = PieceChanged.newBuilder()
                    .addPieces(pieceState)
                    .build()
                logger.info("Joestar emitting")
                emit(
                    GameUpdateEvent.newBuilder()
                        .setPieceChanged(pieceChanged)
                        .build()
                )
            }
        }

    override suspend fun placePiece(request: Coordinate): Empty =
        Empty.getDefaultInstance()
}