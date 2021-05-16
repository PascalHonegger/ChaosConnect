package ch.chaosconnect.joestar.services

import ch.chaosconnect.api.game.Faction
import ch.chaosconnect.api.rohan.GameUpdateResponse
import ch.chaosconnect.api.user.UserAuthResponse
import kotlinx.coroutines.flow.Flow

interface RohanService {
    fun getGameUpdates(): Flow<GameUpdateResponse>
    suspend fun placePiece(column: Int)
    suspend fun startPlaying(faction: Faction)
    suspend fun stopPlaying()
    suspend fun login(username: String, password: String): UserAuthResponse
    suspend fun register(
        displayName: String,
        username: String,
        password: String
    ): UserAuthResponse

    suspend fun playWithoutAccount(
        displayName: String
    ): UserAuthResponse

    suspend fun setDisplayName(newDisplayName: String): UserAuthResponse

    suspend fun setPassword(newPassword: String): UserAuthResponse

    suspend fun renewToken(): UserAuthResponse
}
