package ch.chaosconnect.joestar

import app.cash.turbine.test
import ch.chaosconnect.api.game.GameState
import ch.chaosconnect.api.game.GameStateColumn
import ch.chaosconnect.api.game.GameUpdateEvent
import ch.chaosconnect.api.game.PieceChanged
import ch.chaosconnect.api.rohan.GameUpdateResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class GameStateServiceTest {

    private lateinit var initialGameStateResponse: GameUpdateResponse
    private lateinit var piecePlacedResponse: GameUpdateResponse
    private lateinit var gameUpdates: MutableSharedFlow<GameUpdateResponse>

    private lateinit var rohanService: RohanService
    private lateinit var service: GameStateService

    @BeforeEach
    private fun beforeEach() {
        initialGameStateResponse = GameUpdateResponse.newBuilder().also {
            it.event = GameUpdateEvent.newBuilder().also { builder ->
                builder.gameState = GameState.getDefaultInstance()
            }.build()
            it.newState = it.event.gameState
        }.build()

        piecePlacedResponse = GameUpdateResponse.newBuilder().also {
            it.event = GameUpdateEvent.newBuilder().also { builder ->
                builder.pieceChanged = PieceChanged.getDefaultInstance()
            }.build()
            it.newState = GameState.newBuilder().also { builder ->
                builder.addColumns(
                    GameStateColumn.getDefaultInstance()
                )
            }.build()
        }.build()

        gameUpdates = MutableSharedFlow()
        rohanService = mockk<RohanService>().also {
            every { it.getGameUpdates() } returns gameUpdates
        }
        service = GameStateServiceImpl(rohanService, TestCoroutineScope())
    }

    @Test
    fun `given no events, default game state is emitted`() = runBlocking {
        service.getStateAndUpdates().test {
            assertEquals(
                GameState.getDefaultInstance(),
                expectItem().gameState
            )
            cancel()
        }
    }

    @Test
    fun `given new events, new game state is emitted`() = runBlocking {
        gameUpdates.emit(initialGameStateResponse)
        service.getStateAndUpdates().test {
            assertEquals(GameState.getDefaultInstance(), expectItem().gameState)
            gameUpdates.emit(piecePlacedResponse)
            assertEquals(piecePlacedResponse.event, expectItem())
            cancel()
        }
    }

    @Test
    fun `given existing events, current game state is emitted`() =
        runBlocking {
            gameUpdates.emit(initialGameStateResponse)
            gameUpdates.emit(piecePlacedResponse)
            service.getStateAndUpdates().test {
                assertEquals(
                    piecePlacedResponse.newState,
                    expectItem().gameState
                )
                cancel()
            }
        }

    @Test
    fun `given no events, is not connected`() =
        runBlocking {
            assertFalse(service.isConnected())
        }

    @Test
    fun `given one event, is connected`() =
        runBlocking {
            gameUpdates.emit(initialGameStateResponse)
            assertTrue(service.isConnected())
        }
}