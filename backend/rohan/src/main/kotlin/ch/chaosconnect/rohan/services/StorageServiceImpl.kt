package ch.chaosconnect.rohan.services

import ch.chaosconnect.rohan.meta.StorageConfig
import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.model.TemporaryUser
import ch.chaosconnect.rohan.model.User
import ch.chaosconnect.rohan.model.UserScore
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.runtime.event.annotation.EventListener
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Singleton
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.io.path.*

@ExperimentalPathApi
@ExperimentalStdlibApi
private val logger: Logger =
    LoggerFactory.getLogger(StorageServiceImpl::class.java)

private fun createUniqueIdentifier() =
    UUID.randomUUID().toString()

@ExperimentalPathApi
@ExperimentalStdlibApi
@Singleton
class StorageServiceImpl(config: StorageConfig) :
    StorageService,
    ScheduledStorageService {
    private val lock = ReentrantReadWriteLock()
    private val dataStore = HashMap<String, UserScore>()

    private val storagePath: Path? = when (config.path) {
        null -> null
        else -> Path.of(config.path)
    }

    override fun addUser(processor: (String) -> User): User =
        lock.write {
            val identifier = createUniqueIdentifier()
            val newUser = processor(identifier)
            requireUsernameUnique(newUser)
            dataStore[identifier] = UserScore(user = newUser, score = 0)
            return newUser
        }

    override fun updateUser(
        identifier: String,
        processor: (User) -> User
    ): User =
        lock.write {
            val entry = dataStore[identifier]
            check(entry != null) {
                "Cannot update user with identifier $identifier - no corresponding entry found"
            }
            val updatedUser = processor(entry.user)
            requireUsernameUnique(updatedUser)
            dataStore[identifier] = entry.copy(user = updatedUser)
            return updatedUser
        }

    override fun updateScore(
        identifier: String,
        processor: (Long) -> Long
    ): Long =
        lock.write {
            val entry = dataStore[identifier]
            check(entry != null) {
                "Cannot update score with identifier $identifier - no corresponding entry found"
            }
            val updatedScore = processor(entry.score)
            dataStore[identifier] = entry.copy(score = updatedScore)
            return updatedScore
        }

    override fun getUser(identifier: String): UserScore? =
        lock.read { dataStore[identifier] }

    override fun findUser(
        username: String,
        additionalFilter: (RegularUser) -> Boolean
    ): RegularUser? =
        lock.read {
            dataStore.values
                .map { it.user }
                .filterIsInstance<RegularUser>()
                .find {
                    it.credentials.name.lowercase() == username.lowercase() && additionalFilter(
                        it
                    )
                }
        }

    private fun requireUsernameUnique(changedUser: User): Unit =
        when (changedUser) {
            is RegularUser -> {
                val foundUser = findUser(changedUser.credentials.name)
                check(foundUser == null || foundUser.identifier == changedUser.identifier) {
                    "Username '${changedUser.credentials.name}' already in use"
                }
            }
            is TemporaryUser -> Unit
        }

    private fun store(): Unit = lock.read {
        if (storagePath == null) {
            logger.info("No storage path provided, skip writing data")
            return
        }
        if (storagePath.notExists()) {
            storagePath.createFile()
        }
        if (!storagePath.isWritable()) {
            logger.warn("Cannot write data to $storagePath, skipping")
            return
        }
        try {
            storagePath.writeText(Json.encodeToString(dataStore.values.toList()))
            logger.info("Wrote ${dataStore.size} scores to $storagePath")
        } catch (e: Throwable) {
            logger.error("Failed loading scores from from $storagePath", e)
        }
    }

    private fun load(): Unit = lock.write {
        if (storagePath == null) {
            logger.info("No storage path provided, skip loading data")
            return
        }
        if (!storagePath.isReadable()) {
            logger.warn("Cannot read data at $storagePath, skipping")
            return
        }
        try {
            val parsed =
                Json.decodeFromString<List<UserScore>>(storagePath.readText())
            logger.info("Parsed ${parsed.size} scores from $storagePath")
            dataStore.clear()
            for (userScore in parsed) {
                dataStore[userScore.user.identifier] = userScore
            }
        } catch (e: Throwable) {
            logger.error("Failed loading scores from from $storagePath", e)
        }
    }

    override fun storeDataTick() = store()

    @EventListener
    fun onShutdownEvent(event: ShutdownEvent) = store()

    init {
        load()
    }
}
