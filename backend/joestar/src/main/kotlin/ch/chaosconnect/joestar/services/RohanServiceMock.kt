package ch.chaosconnect.joestar.services

import ch.chaosconnect.api.game.*
import ch.chaosconnect.api.rohan.GameUpdateResponse
import ch.chaosconnect.api.user.UserAuthResponse
import ch.chaosconnect.joestar.auth.currentUserIdentifier
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

private val logger: Logger =
    LoggerFactory.getLogger(RohanServiceMock::class.java)

@Singleton
@Requires(notEnv = ["test"], property = "mocks.rohan")
class RohanServiceMock : RohanService {

    private fun mockRedPiece() = Piece.newBuilder().apply {
        faction = Faction.RED
        owner = "Player1"
    }.build()

    private fun mockYellowPiece() = Piece.newBuilder().apply {
        faction = Faction.YELLOW
        owner = "Player2"
    }.build()

    private fun mockColumn(vararg pieces: Piece) =
        GameStateColumn.newBuilder().apply {
            for (piece in pieces) {
                addPieces(piece)
            }
        }.build()

    private fun mockAuthResponse(userIdentifier: String): UserAuthResponse =
        UserAuthResponse.newBuilder().setIdentifier(userIdentifier).build()

    override fun getGameUpdates(): Flow<GameUpdateResponse> = flow {
        while (true) {
            val initialGameState = GameState.newBuilder().apply {
                numberOfRows = 15
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
                numberOfRows = 20
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
                            column = 5
                            faction = Faction.RED
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

    override suspend fun placePiece(column: Int) = Unit

    override suspend fun login(
        username: String,
        password: String
    ) = mockAuthResponse(UUID.randomUUID().toString())

    override suspend fun register(
        displayName: String,
        username: String,
        password: String
    ) = mockAuthResponse(UUID.randomUUID().toString())

    override suspend fun playWithoutAccount(displayName: String) =
        mockAuthResponse(UUID.randomUUID().toString())

    override suspend fun setDisplayName(newDisplayName: String) =
        mockAuthResponse(currentUserIdentifier.get())

    override suspend fun setPassword(newPassword: String) = mockAuthResponse(
        currentUserIdentifier.get()
    )

    init {
        logger.info("Started mock Rohan implementation")
    }
}
