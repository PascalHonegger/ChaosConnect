package ch.chaosconnect.rohan.services

interface ScheduledGameService {

    suspend fun processQueueTick()

    suspend fun cleanupTick()

    suspend fun resizeFieldTick()
}
