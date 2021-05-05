package ch.chaosconnect.rohan.meta

import ch.chaosconnect.rohan.services.ScheduledGameService
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Singleton
@Requires(notEnv = ["test"])
class GameServiceJobs(private val scheduledGameService: ScheduledGameService) {

    @Scheduled(fixedDelay = "5s")
    fun processQueueTick() = runBlocking {
        scheduledGameService.processQueueTick()
    }

    @Scheduled(fixedDelay = "1m")
    fun cleanupTick() = runBlocking {
        scheduledGameService.cleanupTick()
    }

    @Scheduled(fixedDelay = "30s")
    fun resizeFieldTick() = runBlocking {
        scheduledGameService.resizeFieldTick()
    }
}
