package ch.chaosconnect.joestar

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.Coordinate
import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.GameUpdateResponse
import ch.chaosconnect.api.rohan.UserServiceGrpcKt
import ch.chaosconnect.api.user.AddTemporaryUserRequest
import ch.chaosconnect.api.user.AddUserRequest
import ch.chaosconnect.api.user.GetUserRequest
import ch.chaosconnect.api.user.UpdateUserRequest
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
@Requires(notEnv = ["test"], missingProperty = "mocks.rohan")
class RohanServiceImpl(
    private val gameService: GameServiceGrpcKt.GameServiceCoroutineStub,
    private val userService: UserServiceGrpcKt.UserServiceCoroutineStub
) :
    RohanService {
    override fun getGameUpdates(): Flow<GameUpdateResponse> {
        return gameService.getGameUpdates(Empty.getDefaultInstance())
    }

    override suspend fun placePiece(row: Int, column: Int) {
        gameService.placePiece(
            Coordinate.newBuilder().setRow(row).setColumn(column).build()
        )
    }

    override suspend fun login(username: String, password: String) =
        userService.getUser(
            GetUserRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build()
        )

    override suspend fun register(
        displayName: String,
        username: String,
        password: String
    ) =
        userService.addUser(
            AddUserRequest.newBuilder()
                .setDisplayName(displayName)
                .setUsername(username)
                .setPassword(password)
                .build()
        )

    override suspend fun playWithoutAccount(
        displayName: String
    ) =
        userService.addTemporaryUser(
            AddTemporaryUserRequest.newBuilder()
                .setDisplayName(displayName)
                .build()
        )

    override suspend fun setDisplayName(newDisplayName: String) =
        userService.updateUser(
            UpdateUserRequest.newBuilder()
                .setDisplayName(newDisplayName)
                .build()
        )

    override suspend fun setPassword(newPassword: String) =
        userService.updateUser(
            UpdateUserRequest.newBuilder()
                .setPassword(newPassword)
                .build()
        )
}