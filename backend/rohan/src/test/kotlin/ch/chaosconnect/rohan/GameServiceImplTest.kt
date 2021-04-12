package ch.chaosconnect.rohan

import ch.chaosconnect.api.game.GameState
import ch.chaosconnect.api.game.GameUpdateEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
internal class GameServiceImplTest {

    private lateinit var service: GameService

    @BeforeEach
    fun setUp() {
        service = GameServiceImpl()
    }

    //  FIXME [alexandre]: Ensure test termination
    //      @Test
    fun placePiece_triggersGameUpdates() =
        runBlocking {
            service.placePiece(0, 3)
            Assertions.assertEquals(
                arrayOf<Pair<GameUpdateEvent, GameState>>(
                    Pair(
                        GameUpdateEvent
                            .newBuilder()
                            .build(),
                        GameState
                            .newBuilder()
                            .build()
                    )
                ),
                service
                    .getGameUpdates()
                    .toCollection(ArrayList())
            )
        }

    //  FIXME [alexandre]: Ensure test termination
    //      @Test
    fun getGameUpdates_yieldsInitiallyAnEmptyFlow() =
        runBlocking {
            Assertions.assertEquals(
                arrayOf<Pair<GameUpdateEvent, GameState>>(),
                service.getGameUpdates().toCollection(ArrayList())
            )
        }
}
