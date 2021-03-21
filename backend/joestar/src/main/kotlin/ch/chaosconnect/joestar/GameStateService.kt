package ch.chaosconnect.joestar

import ch.chaosconnect.api.game.GameUpdateEvent
import ch.chaosconnect.api.rohan.GameUpdateResponse
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

interface GameStateService {
    /**
     * Returns a stream of game events, guaranteed to start with a GameState event.
     */
    fun getStateAndUpdates(): Flow<GameUpdateEvent>
}

private val logger: Logger =
    LoggerFactory.getLogger(GameStateServiceImpl::class.java)

private const val timeout = 1000L

private val GameUpdateResponse.asGameStateEvent: GameUpdateEvent
    get() = GameUpdateEvent
        .newBuilder()
        .also { builder -> builder.gameState = this.newState }
        .build()

private val GameUpdateResponse.withEventAsCurrentState: GameUpdateResponse
    get() = GameUpdateResponse
        .newBuilder(this)
        .also { builder -> builder.event = this.asGameStateEvent }
        .build()

@Singleton
@Requires(notEnv = ["test"])
class GameStateServiceImpl(
    private val rohanService: RohanService,
    @param:Named("game_scope") private val coroutineScope: CoroutineScope?
) : GameStateService {

    /**
     * Always contains the latest game update event from Rohan.
     * Emits latest game state on subscription.
     */
    private val flowState =
        MutableStateFlow<GameUpdateResponse>(GameUpdateResponse.getDefaultInstance())

    override fun getStateAndUpdates(): Flow<GameUpdateEvent> =
        flowState.withIndex().map {
            logger.info(it.value.toString())
            if (it.index == 0) it.value.asGameStateEvent
            else it.value.event
        }

    /**
     * Subscribe to [rohanService] and get game updates. If the connection fails or is completed, a new connection is started automatically.
     * On connection, the first received event will be replaced by a game state to ensure the correct state is applied.
     */
    private suspend fun subscribeToRohanUpdates() {
        logger.info("Start listening for Rohan game updates")
        rohanService.getGameUpdates()
            .retry {
                logger.info("Get game update failed (${it.localizedMessage}), re-try in ${timeout}ms")
                delay(timeout)
                return@retry true
            }
            .onCompletion {
                logger.warn("Game update flow completed, restart in ${timeout}ms")
                delay(timeout)
                subscribeToRohanUpdates()
            }
            .withIndex()
            .collect {
                logger.debug("Received new game state from Rohan, action=${it.value.event.actionCase}")
                flowState.emit(
                    if (it.index == 0) it.value.withEventAsCurrentState
                    else it.value
                )
            }
    }

    init {
        (coroutineScope ?: CoroutineScope(Dispatchers.Default)).launch {
            subscribeToRohanUpdates()
        }
    }
}