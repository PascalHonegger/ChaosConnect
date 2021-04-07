package ch.chaosconnect.joestar

import ch.chaosconnect.api.game.*
import ch.chaosconnect.api.rohan.GameUpdateResponse
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val logger: Logger =
    LoggerFactory.getLogger(RohanMockServiceImpl::class.java)

@Singleton
@Requires(notEnv = ["test"], property = "mocks.rohan")
class RohanMockServiceImpl : RohanService {
    init {
        logger.info("Starting mock rohan impl")
    }

    private val redSkin = Skin.newBuilder().apply {
        name = "Default Red Skin"
        faction = Faction.RED
        skinImage = "red.png"
    }.build()

    private val yellowSkin = Skin.newBuilder().apply {
        name = "Default Yellow Skin"
        faction = Faction.YELLOW
        skinImage = "yellow.png"
    }.build()

    private fun mockRedPiece() = Piece.newBuilder().apply {
        skin = redSkin
        owner = "Player1"
    }.build()

    private fun mockYellowPiece() = Piece.newBuilder().apply {
        skin = redSkin
        owner = "Player2"
    }.build()

    private fun mockColumn(vararg pieces: Piece) =
        GameStateColumn.newBuilder().apply {
            for (piece in pieces) {
                addPieces(piece)
            }
        }.build()

    override fun getGameUpdates(): Flow<GameUpdateResponse> = flow {
        while (true) {
            val initialGameState = GameState.newBuilder().apply {
                addAllColumns(
                    listOf(
                        mockColumn(mockRedPiece(), mockRedPiece()),
                        mockColumn(mockYellowPiece()),
                        mockColumn(mockYellowPiece()),
                        mockColumn(mockRedPiece(), mockYellowPiece()),
                        mockColumn(mockYellowPiece(), mockRedPiece()),
                        mockColumn()
                    )
                )
            }.build()

            val updatedGameState = GameState.newBuilder().apply {
                addAllColumns(
                    listOf(
                        mockColumn(mockRedPiece(), mockRedPiece()),
                        mockColumn(mockYellowPiece()),
                        mockColumn(mockYellowPiece()),
                        mockColumn(mockRedPiece(), mockYellowPiece()),
                        mockColumn(mockYellowPiece(), mockRedPiece()),
                        mockColumn(mockRedPiece())
                    )
                )
            }.build()

            val initialState = GameUpdateResponse.newBuilder().apply {
                event = GameUpdateEvent.newBuilder().apply {
                    gameState = initialGameState
                }.build()
                newState = initialGameState
            }.build()

            val updateState = GameUpdateResponse.newBuilder().apply {
                event = GameUpdateEvent.newBuilder().apply {
                    pieceChanged = PieceChanged.newBuilder().apply {
                        addPieces(PieceState.newBuilder().apply {
                            position = Coordinate.newBuilder().setColumn(5).setRow(0).build()
                            skin = redSkin
                            owner = "Player2"
                            action = PieceAction.PLACE
                        }.build())
                    }.build()
                }.build()
                newState = updatedGameState
            }.build()

            logger.info("Emitting dummy values in order")
            emit(initialState)
            delay(3_000)
            emit(updateState)

            delay(10_000)
        }
    }
}