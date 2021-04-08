package ch.chaosconnect.joestar

import ch.chaosconnect.api.rohan.GameUpdateResponse
import ch.chaosconnect.api.user.UserAuthResponse
import kotlinx.coroutines.flow.Flow

interface RohanService {
    fun getGameUpdates(): Flow<GameUpdateResponse>
    suspend fun placePiece(row: Int, column: Int)
    suspend fun login(username: String, password: String): UserAuthResponse
    suspend fun register(
        displayName: String,
        username: String,
        password: String
    ): UserAuthResponse

    suspend fun playWithoutAccount(
        displayName: String
    ): UserAuthResponse

    suspend fun setDisplayName(
        currentUser: String,
        newDisplayName: String
    ): UserAuthResponse

    suspend fun setPassword(
        currentUser: String,
        newPassword: String
    ): UserAuthResponse
}