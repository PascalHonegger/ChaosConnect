package ch.chaosconnect.joestar

import app.cash.turbine.test
import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.game.GameState
import ch.chaosconnect.api.game.GameUpdateEvent
import ch.chaosconnect.api.joestar.ChaosConnectServiceGrpcKt
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
@MicronautTest
class ChaosConnectEndpointTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    lateinit var chaosConnectServiceStub: ChaosConnectServiceGrpcKt.ChaosConnectServiceCoroutineStub

    @get:MockBean(GameStateService::class)
    val gameStateService = mockk<GameStateService>()

    @Test
    fun `service should start`() {
        assertTrue(application.isRunning)
    }

    @Test
    fun `getGameUpdates should return flow from rohan service`() = runBlocking {
        val dummyItem = GameUpdateEvent.newBuilder()
            .apply { gameState = GameState.getDefaultInstance() }.build()
        every { gameStateService.getStateAndUpdates() } returns flow {
            emit(dummyItem)
        }
        chaosConnectServiceStub.getGameUpdates(Empty.getDefaultInstance())
            .test {
                assertEquals(dummyItem, expectItem())
                expectComplete()
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
}