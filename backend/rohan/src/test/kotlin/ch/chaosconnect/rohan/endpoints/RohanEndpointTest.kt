package ch.chaosconnect.rohan.endpoints

import app.cash.turbine.test
import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.*
import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.UserServiceGrpcKt
import ch.chaosconnect.api.user.GetUserRequest
import ch.chaosconnect.api.user.UpdateUserRequest
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
    fun `successful login returns identifier`(): Unit =
        runBlocking {
            coEvery {
                userService.signInAsRegularUser(
                    "john",
                    "123"
                )
            } returns "token"
            val response = userServiceStub.getUser(
                GetUserRequest.newBuilder()
                    .setUsername("john")
                    .setPassword("123").build()
            )

            assertEquals("token", response.identifier)
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
            } returns "token"
            val response = userServiceStub.updateUser(
                UpdateUserRequest.newBuilder()
                    .setPassword("newPw").build()
            )

            coVerify { userService.setPassword("newPw") }

            assertEquals("token", response.identifier)
        }

    @Test
    fun `updateUser with display name sets new name`(): Unit =
        runBlocking {
            coEvery {
                userService.setDisplayName("newName")
            } returns "token"
            val response = userServiceStub.updateUser(
                UpdateUserRequest.newBuilder()
                    .setDisplayName("newName").build()
            )

            coVerify { userService.setDisplayName("newName") }

            assertEquals("token", response.identifier)
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
