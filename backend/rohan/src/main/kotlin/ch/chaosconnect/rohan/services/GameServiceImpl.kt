package ch.chaosconnect.rohan.services

import ch.chaosconnect.api.game.*
import ch.chaosconnect.rohan.meta.userIdentifierContextKey
import ch.chaosconnect.rohan.model.UserScore
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Singleton

const val initialWidth = 7
const val initialHeight = 6
const val inactiveTimeoutMinutes = 30

private data class ActivePlayerState(
    val lastActive: LocalDateTime,
    val faction: Faction?,
    val userScore: UserScore
)

private fun ActivePlayerState.asPlayerState(): PlayerState =
    PlayerState.newBuilder()
        .setFaction(faction)
        .setDisplayName(userScore.user.displayName)
        .setScore(userScore.score)
        .build()

@Singleton
class GameServiceImpl(private val storageService: StorageService) :
    GameService {

    private val mutex = Mutex()
    private val updates = MutableSharedFlow<Pair<GameUpdateEvent, GameState>>(1)
    private val activePlayers = mutableMapOf<String, ActivePlayerState>()
    private val columns = mutableListOf<MutableList<PieceState>>()
    private val queues = mutableListOf<Queue<QueueState>>()
    private val numberOfRows = initialHeight
    private val numberOfColumns get() = columns.size

    override suspend fun startPlaying(faction: Faction): Unit = mutex.withLock {
        val currentUser = userIdentifierContextKey.get()
            ?: error("Cannot start playing without a user")
        val playerState = activePlayers.compute(currentUser) { _, user ->
            when (user) {
                null -> ActivePlayerState(
                    lastActive = LocalDateTime.now(),
                    faction = faction,
                    userScore = storageService.getUser(currentUser)
                        ?: error("User $currentUser not found in storage")
                )
                else -> user.copy(
                    lastActive = LocalDateTime.now(),
                    faction = faction
                )
            }
        } ?: error("User $currentUser not found in activePlayers")

        emitCurrentState {
            playerChanged = PlayerChanged.newBuilder()
                .setAction(PlayerAction.JOIN)
                .setPlayer(currentUser)
                .setState(playerState.asPlayerState())
                .build()
        }
    }

    override suspend fun placePiece(columnIndex: Int): Unit = mutex.withLock {
        val currentUser = userIdentifierContextKey.get()
            ?: error("Cannot place piece without a user")

        require(columnIndex < numberOfColumns) { "Column out of bounds" }

        val user = activePlayers.compute(currentUser) { _, user ->
            user?.copy(lastActive = LocalDateTime.now())
        } ?: error("User $currentUser not actively playing")

        check(queues.none { it.any { queue -> queue.owner == currentUser } }) {
            "User already has piece enqueued"
        }

        check(columns[columnIndex].size < numberOfRows) {
            "Column is already full"
        }

        val state = QueueState
            .newBuilder()
            .setColumn(columnIndex)
            .setFaction(user.faction)
            .setOwner(currentUser)
            .build()

        queues[columnIndex].add(state)

        emitCurrentState {
            queueChanged = QueueChanged
                .newBuilder()
                .addPieces(state)
                .build()
        }
    }

    @Scheduled(fixedDelay = "5s")
    fun processQueueTick() = runBlocking {
        mutex.withLock {
            val queueToProcess =
                queues.filter { it.isNotEmpty() }.randomOrNull()
                    ?: return@withLock

            val enqueueTask = queueToProcess.poll()
            val column = columns[enqueueTask.column]
            val state = PieceState.newBuilder()
                .setAction(PieceAction.PLACE)
                .setFaction(enqueueTask.faction)
                .setOwner(enqueueTask.owner)
                .setColumn(enqueueTask.column)
                .build()
            column.add(state)

            if (column.size == numberOfRows - 1) {
                queues[enqueueTask.column].clear()
            }

            emitCurrentState {
                pieceChanged = PieceChanged
                    .newBuilder()
                    .addPieces(state)
                    .build()
            }
        }
    }

    @Scheduled(fixedDelay = "1m")
    fun cleanupTick() = runBlocking {
        mutex.withLock {
            val inactivePlayers = activePlayers.filterValues { player ->
                player.lastActive.until(
                    LocalDateTime.now(),
                    ChronoUnit.MINUTES
                ) > inactiveTimeoutMinutes
            }
            for (id in inactivePlayers.keys) {
                activePlayers.remove(id)
                emitCurrentState {
                    playerChanged = PlayerChanged.newBuilder()
                        .setAction(PlayerAction.DISCONNECT)
                        .setPlayer(id)
                        .build()
                }
            }
        }
    }

    @Scheduled(fixedDelay = "30s")
    fun resizeFieldTick() = runBlocking {
        mutex.withLock {
            // TODO Some logic with amount of active players
//                emitCurrentState {
//                    rowChanged = RowChanged
//                        .newBuilder()
//                        .setPosition(32)
//                        .setAction(RowColumnAction.ADD)
//                        .build()
//                }
        }
    }

    init {
        repeat(initialWidth) { columns.add(mutableListOf()) }
        repeat(initialWidth) { queues.add(LinkedList()) }
        runBlocking { emitCurrentState() }
    }

    override fun getGameUpdates(): Flow<Pair<GameUpdateEvent, GameState>> =
        updates

    private suspend fun emitCurrentState(setChangeReason: (GameUpdateEvent.Builder.() -> Unit)? = null) {
        val state =
            GameState
                .newBuilder()
                .addAllColumns(
                    (0 until numberOfColumns).map { index ->
                        GameStateColumn
                            .newBuilder()
                            .addAllPieces(
                                columns[index].map {
                                    Piece
                                        .newBuilder()
                                        .setOwner(it.owner)
                                        .setFaction(it.faction)
                                        .build()
                                })
                            .addAllQueue(
                                queues[index].map {
                                    Piece
                                        .newBuilder()
                                        .setOwner(it.owner)
                                        .setFaction(it.faction)
                                        .build()
                                })
                            .setDisabled(index % 3 == 0) // TODO
                            .build()
                    }
                )
                .setNumberOfRows(numberOfRows)
                .putAllPlayers(activePlayers.mapValues { (_, player) -> player.asPlayerState() })
                .build()
        val eventBuilder = GameUpdateEvent.newBuilder()
        if (setChangeReason != null) {
            eventBuilder.setChangeReason()
        } else {
            eventBuilder.gameState = state
        }
        updates.emit(eventBuilder.build() to state)
    }
}
