package ch.chaosconnect.rohan.services

import app.cash.turbine.test
import ch.chaosconnect.api.game.*
import ch.chaosconnect.rohan.model.TemporaryUser
import ch.chaosconnect.rohan.model.UserScore
import ch.chaosconnect.rohan.runSignedIn
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class GameServiceImplTest {

    private lateinit var service: GameServiceImpl
    private lateinit var storage: StorageService

    private fun dummyScore(id: String) =
        UserScore(user = TemporaryUser(id, "Dummy User"), 42)

    @BeforeEach
    fun setUp() {
        storage = mockk()
        service = GameServiceImpl(storage)
    }

    @Test
    fun `placePiece triggers game updates`() =
        runSignedIn("my-user") {
            val score = dummyScore("my-user")
            every { storage.getUser("my-user") } returns score
            service.startPlaying(Faction.YELLOW)
            service.placePiece(3)
            service.getGameUpdates().test {
                val (event, state) = expectItem()
                assertEquals(
                    GameUpdateEvent.newBuilder().setQueueChanged(
                        QueueChanged
                            .newBuilder()
                            .addPieces(
                                QueueState.newBuilder()
                                    .setColumn(3)
                                    .setOwner("my-user")
                                    .setFaction(Faction.YELLOW)
                                    .build()
                            )
                    ).build(),
                    event
                )
                assertEquals(
                    GameState
                        .newBuilder()
                        .addColumns(
                            GameStateColumn.newBuilder().setDisabled(true)
                        )
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(
                            GameStateColumn.newBuilder().setDisabled(true)
                                .addQueue(
                                    Piece.newBuilder().setOwner("my-user")
                                        .setFaction(Faction.YELLOW)
                                )
                        )
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(GameStateColumn.newBuilder())
                        .addColumns(
                            GameStateColumn.newBuilder().setDisabled(true)
                        )
                        .putPlayers(
                            "my-user",
                            PlayerState.newBuilder()
                                .setDisplayName(score.user.displayName)
                                .setScore(score.score)
                                .setFaction(Faction.YELLOW)
                                .build()
                        )
                        .setNumberOfRows(6)
                        .build(),
                    state
                )
                expectNoEvents()
                cancel()
            }
        }

    @Test
    fun `getGameUpdates initially yields an empty flow`() =
        runBlocking {
            service.getGameUpdates().test {
                val (event, state) = expectItem()
                assertEquals(state, event.gameState)
                assertEquals(7, state.columnsCount)
                expectNoEvents()
                cancel()
            }
        }

    @Test
    fun `processQueueTick TODO`() {
        //  TODO: Add tests
    }

    @Test
    fun `cleanupTick TODO`() {
        //  TODO: Add tests
    }

    @Test
    fun `resizeFieldTick TODO`() {
        //  TODO: Add tests
    }
}
