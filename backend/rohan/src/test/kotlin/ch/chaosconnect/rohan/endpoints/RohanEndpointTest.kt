package ch.chaosconnect.rohan.endpoints

import app.cash.turbine.test
import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.*
import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.UserServiceGrpcKt
import ch.chaosconnect.api.user.GetUserRequest
import ch.chaosconnect.api.user.PlayerType
import ch.chaosconnect.api.user.UpdateUserRequest
import ch.chaosconnect.api.user.UserTokenContent
import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.model.TemporaryUser
import ch.chaosconnect.rohan.model.UserCredentials
import ch.chaosconnect.rohan.services.GameService
import ch.chaosconnect.rohan.services.UserService
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
@MicronautTest
internal class RohanEndpointTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    lateinit var gameServiceStub: GameServiceGrpcKt.GameServiceCoroutineStub

    @Inject
    lateinit var userServiceStub: UserServiceGrpcKt.UserServiceCoroutineStub

    @get:MockBean(GameService::class)
    val gameService = mockk<GameService>()

    @get:MockBean(UserService::class)
    val userService = mockk<UserService>()

    @Test
    fun `service should start`() {
        assertTrue(application.isRunning)
    }

    @Test
    fun `successful login returns token content`(): Unit =
        runBlocking {
            coEvery {
                userService.signInAsRegularUser(
                    "john",
                    "123"
                )
            } returns TemporaryUser("identifier", "some name")
            val response = userServiceStub.getUser(
                GetUserRequest.newBuilder()
                    .setUsername("john")
                    .setPassword("123").build()
            )

            val expectedResponse = UserTokenContent.newBuilder()
                .setIdentifier("identifier")
                .setPlayerType(PlayerType.TEMPORARY)
                .build()
            assertEquals(expectedResponse, response.token)
        }

    @Test
    fun `failed login returns error`(): Unit =
        runBlocking {
            coEvery {
                userService.signInAsRegularUser(
                    "john",
                    "123"
                )
            } throws IllegalStateException("Some Error")
            val response = userServiceStub.getUser(
                GetUserRequest.newBuilder()
                    .setUsername("john")
                    .setPassword("123").build()
            )

            assertEquals("Some Error", response.failureReason)
        }

    @Test
    fun `updateUser with password sets new password`(): Unit =
        runBlocking {
            coEvery {
                userService.setPassword("newPw")
            } returns RegularUser("identifier", "some name", UserCredentials("username", "password-hash"))
            val response = userServiceStub.updateUser(
                UpdateUserRequest.newBuilder()
                    .setPassword("newPw").build()
            )

            coVerify { userService.setPassword("newPw") }

            val expectedResponse = UserTokenContent.newBuilder()
                .setIdentifier("identifier")
                .setPlayerType(PlayerType.REGULAR)
                .build()
            assertEquals(expectedResponse, response.token)
        }

    @Test
    fun `updateUser with display name sets new name`(): Unit =
        runBlocking {
            coEvery {
                userService.setDisplayName("newName")
            } returns TemporaryUser("identifier", "some name")
            val response = userServiceStub.updateUser(
                UpdateUserRequest.newBuilder()
                    .setDisplayName("newName").build()
            )

            coVerify { userService.setDisplayName("newName") }

            val expectedResponse = UserTokenContent.newBuilder()
                .setIdentifier("identifier")
                .setPlayerType(PlayerType.TEMPORARY)
                .build()
            assertEquals(expectedResponse, response.token)
        }

    @Test
    fun `renew token works for existing user`(): Unit =
        runBlocking {
            coEvery {
                userService.getCurrentUser()
            } returns TemporaryUser("identifier", "some name")
            val response = userServiceStub.renewToken(Empty.getDefaultInstance())

            val expectedResponse = UserTokenContent.newBuilder()
                .setIdentifier("identifier")
                .setPlayerType(PlayerType.TEMPORARY)
                .build()
            assertEquals(expectedResponse, response.token)
        }

    @Test
    fun `renew token fails for inexistent user`(): Unit =
        runBlocking {
            coEvery {
                userService.getCurrentUser()
            } throws IllegalStateException("Some Error")
            val response = userServiceStub.renewToken(Empty.getDefaultInstance())
            assertEquals("Some Error", response.failureReason)
        }

    @Test
    fun `placePiece places piece`(): Unit =
        runBlocking {
            coJustRun { gameService.placePiece(columnIndex = 3) }
            gameServiceStub.placePiece(
                PlacePieceRequest.newBuilder().setColumn(3).build()
            )
            coVerify { gameService.placePiece(columnIndex = 3) }
        }

    @Test
    fun `startPlaying starts a session`(): Unit =
        runBlocking {
            coJustRun { gameService.startPlaying(faction = Faction.YELLOW) }
            gameServiceStub.startPlaying(
                StartPlayingRequest.newBuilder()
                    .setFaction(Faction.YELLOW).build()
            )
            coVerify { gameService.startPlaying(faction = Faction.YELLOW) }
        }

    @Test
    fun `getGameUpdates returns game updates`(): Unit = runBlocking {
        val event = GameUpdateEvent.newBuilder().build()
        val state = GameState.newBuilder().build()
        every { gameService.getGameUpdates() } returns flow {
            emit(event to state)
        }

        gameServiceStub.getGameUpdates(Empty.getDefaultInstance()).test {
            val item = expectItem()
            assertEquals(event, item.event)
            assertEquals(state, item.newState)
            expectComplete()
        }
    }

    @Test
    fun `stopPlaying stops session`(): Unit =
        runBlocking {
            coJustRun { gameService.stopPlaying() }
            gameServiceStub.stopPlaying(Empty.getDefaultInstance())
            coVerify { gameService.stopPlaying() }
        }
}

@Factory
internal class Clients {
    @Bean
    fun gameServiceStub(
        @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
    ): GameServiceGrpcKt.GameServiceCoroutineStub {
        return GameServiceGrpcKt.GameServiceCoroutineStub(
            channel
        )
    }

    @Bean
    fun userServiceStub(
        @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
    ): UserServiceGrpcKt.UserServiceCoroutineStub {
        return UserServiceGrpcKt.UserServiceCoroutineStub(
            channel
        )
    }
}
