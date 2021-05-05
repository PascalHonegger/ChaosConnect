package ch.chaosconnect.rohan.services

interface ScheduledGameService {

    fun processQueueTick()

    fun cleanupTick()

    fun resizeFieldTick()
}
