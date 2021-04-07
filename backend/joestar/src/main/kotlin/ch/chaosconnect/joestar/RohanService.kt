package ch.chaosconnect.joestar

import ch.chaosconnect.api.rohan.GameUpdateResponse
import kotlinx.coroutines.flow.Flow

interface RohanService {
    suspend fun echo(message: String): String
    fun getGameUpdates(): Flow<GameUpdateResponse>
}