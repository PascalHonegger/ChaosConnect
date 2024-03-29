package ch.chaosconnect.joestar.endpoints

import ch.chaosconnect.api.authentication.LoginRequest
import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.PlacePieceRequest
import ch.chaosconnect.api.game.StartPlayingRequest
import ch.chaosconnect.api.joestar.ChaosConnectServiceGrpcKt
import ch.chaosconnect.api.joestar.WebLoginServiceGrpcKt
import ch.chaosconnect.api.user.UserAuthResponse
import ch.chaosconnect.api.user.UserTokenContent
import ch.chaosconnect.joestar.services.GameStateService
import ch.chaosconnect.joestar.services.RohanService
import io.grpc.ManagedChannel
import io.grpc.StatusException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
@MicronautTest
class JoestarEndpointTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    lateinit var chaosConnectServiceStub: ChaosConnectServiceGrpcKt.ChaosConnectServiceCoroutineStub

    @Inject
    lateinit var webLoginStub: WebLoginServiceGrpcKt.WebLoginServiceCoroutineStub

    @get:MockBean(GameStateService::class)
    val gameStateService = mockk<GameStateService>()

    @get:MockBean(RohanService::class)
    val rohanService = mockk<RohanService>()

    @Test
    fun `service should start`() {
        assertTrue(application.isRunning)
    }

    @Test
    fun `placePiece throws unauthenticated exception`(): Unit =
        runBlocking {
            assertThrows<StatusException> {
                chaosConnectServiceStub.placePiece(PlacePieceRequest.getDefaultInstance())
            }
        }

    @Test
    fun `startPlaying throws unauthenticated exception`(): Unit =
        runBlocking {
            assertThrows<StatusException> {
                chaosConnectServiceStub.startPlaying(StartPlayingRequest.getDefaultInstance())
            }
        }

    @Test
    fun `login returns token`(): Unit = runBlocking {
        coEvery {
            rohanService.login(
                "User",
                "Password"
            )
        } returns UserAuthResponse.newBuilder()
            .setToken(UserTokenContent.newBuilder().setIdentifier("123456"))
            .build()

        val request = LoginRequest.newBuilder()
            .setUsername("User")
            .setPassword("Password")
            .build()
        val response = webLoginStub.login(request)
        assertFalse(
            response.jwtToken.isNullOrEmpty(),
            "Expected non empty token"
        )
        coVerify {
            rohanService.login(
                "User",
                "Password"
            )
        }
    }

    @Test
    fun `stopPlaying throws unauthenticated exception`(): Unit = runBlocking {
        assertThrows<StatusException> {
            chaosConnectServiceStub.stopPlaying(Empty.getDefaultInstance())
        }
    }
}

@Factory
internal class Clients {
    @Bean
    fun chaosConnectServiceStub(
        @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
    ): ChaosConnectServiceGrpcKt.ChaosConnectServiceCoroutineStub {
        return ChaosConnectServiceGrpcKt.ChaosConnectServiceCoroutineStub(
            channel
        )
    }

    @Bean
    fun webLoginStub(
        @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
    ): WebLoginServiceGrpcKt.WebLoginServiceCoroutineStub {
        return WebLoginServiceGrpcKt.WebLoginServiceCoroutineStub(
            channel
        )
    }
}
