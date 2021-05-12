package ch.chaosconnect.rohan.services

import ch.chaosconnect.api.game.*
import ch.chaosconnect.rohan.meta.userIdentifierContextKey
import ch.chaosconnect.rohan.model.*
import ch.chaosconnect.rohan.utilities.calculateCollectionResizingSuggestions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.sign

const val initialWidth = 7
const val initialHeight = 6
const val inactiveTimeoutMinutes = 30

private data class ActivePlayerState(
    val lastActive: LocalDateTime,
    val faction: Faction,
    val userScore: UserScore
) {
    fun toPlayerState(): PlayerState =
        PlayerState.newBuilder()
            .setFaction(faction)
            .setDisplayName(userScore.user.displayName)
            .setScore(userScore.score)
            .build()
}

@Singleton
class GameServiceImpl(private val storageService: StorageService) :
    GameService,
    ScheduledGameService {

    private val mutex = Mutex()
    private val updates = MutableSharedFlow<Pair<GameUpdateEvent, GameState>>(1)
    private val activePlayers = mutableMapOf<String, ActivePlayerState>()
    private val columns = mutableListOf<GameColumn>()
    private val queues = mutableListOf<GameColumnQueue>()
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
                .setState(playerState.toPlayerState())
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

        val state = QueueCell(owner = currentUser, faction = user.faction)

        queues[columnIndex].add(state)

        emitCurrentState {
            queueChanged = QueueChanged
                .newBuilder()
                .addPieces(state.toQueueState(columnIndex))
                .build()
        }
    }

    override suspend fun processQueueTick() =
        mutex.withLock {
            val (columnIndex, queueToProcess) =
                queues.withIndex()
                    .filter { it.value.isNotEmpty() }
                    .randomOrNull()
                    ?: return@withLock

            val enqueueTask = queueToProcess.poll()
            val column = columns[columnIndex]
            val state = GameCell(
                owner = enqueueTask.owner,
                faction = enqueueTask.faction,
                scored = false
            )
            column.add(state)
            val rowIndex = column.size - 1

            if (column.size == numberOfRows - 1) {
                queueToProcess.clear()
            }

            emitCurrentState {
                pieceChanged = PieceChanged
                    .newBuilder()
                    .addPieces(
                        state.toPieceState(
                            action = PieceAction.PLACE,
                            columnIndex = columnIndex,
                            rowIndex = rowIndex
                        )
                    )
                    .build()
            }

            val winningPieces = getWinningPieces(
                gameBoard = columns,
                placedColumn = columnIndex,
                placedRow = rowIndex
            )

            if (winningPieces.isNotEmpty()) {
                val addedScores = mutableMapOf<String, Long>()

                // Player who placed last piece gets one point per piece
                addedScores[state.owner] = winningPieces.size.toLong()

                // Additionally give one piece to each owner of every piece
                for ((piece, pieceColumn, pieceRow) in winningPieces) {
                    columns.getPieceOrNull(column = pieceColumn, row = pieceRow)
                    columns[pieceColumn][pieceRow] = piece.copy(scored = true)
                    addedScores.compute(piece.owner) { _, score ->
                        (score ?: 0) + 1
                    }
                }

                // Emit piece changes as they're now marked as scored
                emitCurrentState {
                    pieceChanged = PieceChanged
                        .newBuilder()
                        .addAllPieces(
                            winningPieces.map {
                                it.piece.toPieceState(
                                    action = PieceAction.SCORE,
                                    columnIndex = it.column,
                                    rowIndex = it.row
                                )
                            }
                        )
                        .build()
                }

                // Add scores to users and updated users
                for ((user, addedScore) in addedScores) {
                    val updatedUserScore =
                        storageService.updateScore(user) { it + addedScore }
                    val updatedUser = activePlayers.compute(user) { _, player ->
                        player?.copy(userScore = updatedUserScore)
                    }
                    if (updatedUser != null) {
                        emitCurrentState {
                            playerChanged = PlayerChanged
                                .newBuilder()
                                .setPlayer(user)
                                .setAction(PlayerAction.UPDATE)
                                .setState(updatedUser.toPlayerState())
                                .build()
                        }
                    }
                }
            }

        }

    override suspend fun cleanupTick() =
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

    override suspend fun resizeFieldTick() =
        mutex.withLock {
            val targetColumnCount = max(activePlayers.size, 2) * 3 + 1
            assert(columns.size == queues.size) {
                "Column count (${columns.size}) is not queue count (${queues.size})"
            }
            val (columnsHeadSuggestion, columnsTailSuggestion) = calculateCollectionResizingSuggestions(
                columns,
                targetColumnCount
            ) {
                it.isNotEmpty()
            }
            val (queuesHeadSuggestion, queuesTailSuggestion) = calculateCollectionResizingSuggestions(
                queues,
                targetColumnCount
            ) {
                it.isNotEmpty()
            }
            val headSuggestion =
                max(columnsHeadSuggestion, queuesHeadSuggestion)
            val tailSuggestion =
                max(columnsTailSuggestion, queuesTailSuggestion)

            // Add new columns
            val addedIndices = mutableListOf<Int>()
            if (headSuggestion > 0) {
                repeat(headSuggestion) {
                    columns.add(0, CompleteColumn())
                    addedIndices += it
                }
            }
            if (tailSuggestion > 0) {
                repeat(tailSuggestion) {
                    addedIndices += columns.size
                    columns.add(CompleteColumn())
                }
            }
            if (addedIndices.isNotEmpty()) {
                emitCurrentState {
                    columnChanged = ColumnChanged
                        .newBuilder()
                        .addAllPositions(addedIndices)
                        .setAction(ColumnAction.ADD)
                        .build()
                }
            }

            // Delete existing columns
            val deletedIndices = mutableListOf<Int>()
            if (headSuggestion < 0) {
                repeat(-headSuggestion) {
                    columns.removeFirst()
                    deletedIndices += it
                }
            }
            if (tailSuggestion < 0) {
                repeat(-tailSuggestion) {
                    columns.removeLast()
                    deletedIndices += columns.size
                }
            }
            if (deletedIndices.isNotEmpty()) {
                emitCurrentState {
                    columnChanged = ColumnChanged
                        .newBuilder()
                        .addAllPositions(deletedIndices)
                        .setAction(ColumnAction.DELETE)
                        .build()
                }
            }
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
                                        .setScored(it.scored)
                                        .build()
                                })
                            .addAllQueue(
                                queues[index].map {
                                    Piece
                                        .newBuilder()
                                        .setOwner(it.owner)
                                        .setFaction(it.faction)
                                        .setScored(false)
                                        .build()
                                })
                            .setDisabled(index % 3 == 0) // TODO
                            .build()
                    }
                )
                .setNumberOfRows(numberOfRows)
                .putAllPlayers(activePlayers.mapValues { (_, player) -> player.toPlayerState() })
                .build()
        val eventBuilder = GameUpdateEvent.newBuilder()
        if (setChangeReason != null) {
            eventBuilder.setChangeReason()
        } else {
            eventBuilder.gameState = state
        }
        updates.emit(eventBuilder.build() to state)
    }

    init {
        repeat(initialWidth) { columns.add(mutableListOf()) }
        repeat(initialWidth) { queues.add(LinkedList()) }
        runBlocking { emitCurrentState() }
    }
}
