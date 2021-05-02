package ch.chaosconnect.joestar.services

import ch.chaosconnect.api.game.Faction
import ch.chaosconnect.api.game.GameUpdateEvent
import kotlinx.coroutines.flow.Flow

interface GameStateService {
    /**
     * Returns a stream of [GameUpdateEvent], guaranteed to start with a GameState event.
     */
    fun getStateAndUpdates(): Flow<GameUpdateEvent>

    /**
     * Place a piece at the requested coordinate
     */
    suspend fun placePiece(column: Int)

    /**
     * Start the game session with the specified [Faction]
     */
    suspend fun startPlaying(faction: Faction)

    /**
     * Indicate whether a healthy connection to the Rohan exists.
     */
    fun isConnected(): Boolean
}
