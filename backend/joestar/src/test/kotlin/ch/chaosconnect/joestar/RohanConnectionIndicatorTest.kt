package ch.chaosconnect.joestar

import io.micronaut.health.HealthStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RohanConnectionIndicatorTest {
    @Test
    fun `connected service is up`() = runBlocking {
        val gameStateService = mockk<GameStateService>()
        val rohanConnectionIndicator = RohanConnectionIndicator(gameStateService)
        every { gameStateService.isConnected() } returns true
        assertEquals(HealthStatus.UP, rohanConnectionIndicator.result.awaitSingle().status)
    }

    @Test
    fun `disconnected service is down`() = runBlocking {
        val gameStateService = mockk<GameStateService>()
        val rohanConnectionIndicator = RohanConnectionIndicator(gameStateService)
        every { gameStateService.isConnected() } returns false
        assertEquals(HealthStatus.DOWN, rohanConnectionIndicator.result.awaitSingle().status)
    }
}
