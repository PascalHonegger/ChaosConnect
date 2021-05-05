package ch.chaosconnect.rohan.services

interface ScheduledStorageService {

    suspend fun storeDataTick()
}
