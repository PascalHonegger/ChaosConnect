package ch.chaosconnect.joestar

import ch.chaosconnect.api.rohan.GameUpdateResponse
import kotlinx.coroutines.flow.Flow

interface RohanService {
    fun getGameUpdates(): Flow<GameUpdateResponse>
}