package ch.chaosconnect.joestar

import app.cash.turbine.test
import ch.chaosconnect.api.game.*
import ch.chaosconnect.api.rohan.GameUpdateResponse
import io.mockk.coJustRun
import io.mockk.coVerify
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
        initialGameStateResponse = GameUpdateResponse.newBuilder().apply {
            event = GameUpdateEvent.newBuilder().apply {
                gameState = GameState.getDefaultInstance()
            }.build()
            newState = event.gameState
        }.build()

        piecePlacedResponse = GameUpdateResponse.newBuilder().apply {
            event = GameUpdateEvent.newBuilder().apply {
                pieceChanged = PieceChanged.getDefaultInstance()
            }.build()
            newState = GameState.newBuilder().apply {
                addColumns(
                    GameStateColumn.getDefaultInstance()
                )
            }.build()
        }.build()

        gameUpdates = MutableSharedFlow()
        rohanService = mockk<RohanService>().apply {
            every { getGameUpdates() } returns gameUpdates
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
            expectNoEvents()
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

    @Test
    fun `placePiece is forwarded to Rohan`() =
        runBlocking {
            coJustRun { rohanService.placePiece(row = 1, column = 2) }
            service.placePiece(
                Coordinate.newBuilder().setRow(1).setColumn(2).build()
            )
            coVerify { rohanService.placePiece(row = 1, column = 2) }
        }
}
