package ch.chaosconnect.joestar.meta

import ch.chaosconnect.joestar.services.GameStateService
import io.micronaut.context.annotation.Requires
import io.micronaut.health.HealthStatus
import io.micronaut.management.endpoint.health.HealthEndpoint
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.management.health.indicator.annotation.Readiness
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher
import javax.inject.Singleton

@Singleton
@Requires(notEnv = ["test"], beans = [HealthEndpoint::class])
@Readiness
class RohanConnectionIndicator(
    private val gameStateService: GameStateService
) : HealthIndicator {
    override fun getResult(): Publisher<HealthResult> = flow {
        val status = when {
            gameStateService.isConnected() -> HealthStatus.UP
            else -> HealthStatus.DOWN
        }
        emit(
            HealthResult.builder("rohan-connection", status).build()
        )
    }.asPublisher()
}
