package ch.chaosconnect.rohan.services

import io.micronaut.scheduling.annotation.Scheduled

interface ScheduledStorageService {

    @Scheduled(initialDelay = "1m", fixedDelay = "15m")
    fun storeDataTick()
}
