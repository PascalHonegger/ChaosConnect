package ch.chaosconnect.rohan

import app.cash.turbine.test
import ch.chaosconnect.api.game.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
internal class GameServiceImplTest {

    private lateinit var service: GameService

    @BeforeEach
    fun setUp() {
        service = GameServiceImpl()
    }

    @Test
    fun `placePiece triggers game updates`() =
        runSignedIn("my-user") {
            service.placePiece(0, 3)
            service.getGameUpdates().test {
                val (event, state) = expectItem()
                assertEquals(
                    GameUpdateEvent.newBuilder().setPieceChanged(
                        PieceChanged
                            .newBuilder()
                            .addPieces(
                                PieceState.newBuilder()
                                    .setAction(PieceAction.PLACE)
                                    .setPosition(
                                        Coordinate.newBuilder().setRow(0)
                                            .setColumn(3).build()
                                    )
                                    .setOwner("my-user")
                                    .build()
                            )
                            .build()
                    )
                        .build(),
                    event
                )
                assertEquals(
                    state,
                    GameState
                        .newBuilder()
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(
                            GameStateColumn.newBuilder().addPieces(
                                Piece.newBuilder().setOwner("my-user")
                                    .setSkin(Skin.newBuilder())
                            )
                        )
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(GameStateColumn.newBuilder())
                        .build()
                )
                expectNoEvents()
                cancel()
            }
        }

    @Test
    fun `getGameUpdates yields initially an empty flow`() =
        runBlocking {
            service.getGameUpdates().test {
                val (event, state) = expectItem()
                val expectedState = GameState.getDefaultInstance()
                assertEquals(expectedState, state)
                assertEquals(expectedState, event.gameState)
                expectNoEvents()
                cancel()
            }
        }
}
