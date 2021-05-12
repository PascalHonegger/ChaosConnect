package ch.chaosconnect.rohan.services

interface ScheduledGameService {

    suspend fun processQueueTick()

    suspend fun cleanupUsersTick()

    suspend fun clearColumnsTick()

    suspend fun resizeFieldTick()
}
