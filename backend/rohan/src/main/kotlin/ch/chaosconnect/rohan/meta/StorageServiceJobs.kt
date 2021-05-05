package ch.chaosconnect.rohan.meta

import ch.chaosconnect.rohan.services.ScheduledStorageService
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Singleton
@Requires(notEnv = ["test"])
class StorageServiceJobs(private val scheduledStorageService: ScheduledStorageService) {

    @Scheduled(initialDelay = "1m", fixedDelay = "15m")
    fun storeDataTick() = runBlocking {
        scheduledStorageService.storeDataTick()
    }
}
