package ch.chaosconnect.rohan.services

import ch.chaosconnect.api.game.Faction
import ch.chaosconnect.api.game.GameState
import ch.chaosconnect.api.game.GameUpdateEvent
import kotlinx.coroutines.flow.Flow

interface GameService {
    suspend fun startPlaying(faction: Faction)
    suspend fun placePiece(columnIndex: Int)

    fun getGameUpdates(): Flow<Pair<GameUpdateEvent, GameState>>
}
