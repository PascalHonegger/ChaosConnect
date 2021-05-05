package ch.chaosconnect.rohan.meta

import ch.chaosconnect.rohan.services.ScheduledGameService
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import javax.inject.Singleton

@Singleton
@Requires(notEnv = ["test"])
class GameServiceJobs(private val scheduledGameService: ScheduledGameService) :
    ScheduledGameService {

    @Scheduled(fixedDelay = "5s")
    override fun processQueueTick() =
        scheduledGameService.processQueueTick()

    @Scheduled(fixedDelay = "1m")
    override fun cleanupTick() =
        scheduledGameService.cleanupTick()

    @Scheduled(fixedDelay = "30s")
    override fun resizeFieldTick() =
        scheduledGameService.resizeFieldTick()
}
