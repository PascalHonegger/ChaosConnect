package ch.chaosconnect.rohan

import ch.chaosconnect.api.game.GameState
import ch.chaosconnect.api.game.GameUpdateEvent
import kotlinx.coroutines.flow.Flow

interface GameService {

    fun placePiece(rowIndex: Int, columnIndex: Int)

    fun getGameUpdates(): Flow<Pair<GameUpdateEvent, GameState>>
}
