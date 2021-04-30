package ch.chaosconnect.joestar.services

import ch.chaosconnect.api.game.Coordinate
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
    suspend fun placePiece(coordinate: Coordinate)

    /**
     * Indicate whether a healthy connection to the Rohan exists.
     */
    fun isConnected(): Boolean
}
