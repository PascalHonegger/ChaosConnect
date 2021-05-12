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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Singleton
import kotlin.math.max

const val initialWidth = 7
const val initialHeight = 6
const val inactiveTimeoutMinutes = 30
const val disabledUntilClearedTimeoutSeconds = 30

private val logger: Logger =
    LoggerFactory.getLogger(GameServiceImpl::class.java)

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
    private val columns = mutableListOf<CompleteColumn>()
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

        check(columns.none { it.queue.any { queue -> queue.owner == currentUser } }) {
            "User already has piece enqueued"
        }

        check(columns[columnIndex].numRows < numberOfRows) {
            "Column is already full"
        }

        check(columns[columnIndex].isEnabled) {
            "Column is disabled"
        }

        val state = QueueCell(owner = currentUser, faction = user.faction)

        columns[columnIndex].enqueue(state)

        emitCurrentState {
            queueChanged = QueueChanged
                .newBuilder()
                .addPieces(state.toQueueState(columnIndex))
                .build()
        }
    }

    override suspend fun processQueueTick() =
        mutex.withLock {
            val (columnIndex, column) =
                columns.withIndex()
                    .filter { it.value.hasQueue() }
                    .randomOrNull()
                    ?: return

            val state = column.dequeue().run {
                GameCell(
                    owner = owner,
                    faction = faction,
                    scored = false
                )
            }
            column.place(state)
            val rowIndex = column.rows.size - 1

            val columnIndicesToDisable = mutableSetOf<Int>()

            if (column.rows.size == numberOfRows - 1) {
                column.queue.clear()
                columnIndicesToDisable += columnIndex
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
                gameBoard = columns.onlyRows(),
                placedColumn = columnIndex,
                placedRow = rowIndex
            )

            if (winningPieces.isNotEmpty()) {
                val addedScores = mutableMapOf<String, Long>()

                // Player who placed last piece gets one point per piece
                addedScores[state.owner] = winningPieces.size.toLong()

                // Additionally give one piece to each owner of every piece
                for ((piece, pieceColumn, pieceRow) in winningPieces) {
                    columns[pieceColumn].rows[pieceRow] =
                        piece.copy(scored = true)
                    addedScores.compute(piece.owner) { _, score ->
                        (score ?: 0) + 1
                    }
                    columnIndicesToDisable += pieceColumn
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

            if (columnIndicesToDisable.isNotEmpty()) {
                val disabledAt = LocalDateTime.now()
                for (colToDisable in columnIndicesToDisable) {
                    columns[colToDisable].disabledAt = disabledAt
                }
                emitCurrentState {
                    columnChanged = ColumnChanged
                        .newBuilder()
                        .setAction(ColumnAction.DISABLE)
                        .addAllPositions(columnIndicesToDisable)
                        .build()
                }
            }
        }

    override suspend fun cleanupUsersTick() =
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

    override suspend fun clearColumnsTick() =
        mutex.withLock {
            val columnsToClear = columns.withIndex().filter { column ->
                column.value.disabledAt != null &&
                        (column.value.disabledAt!!.until(
                            LocalDateTime.now(),
                            ChronoUnit.SECONDS
                        ) > disabledUntilClearedTimeoutSeconds)
            }
            if (columnsToClear.isNotEmpty()) {
                for ((_, column) in columnsToClear) {
                    column.reset()
                }
                emitCurrentState {
                    columnChanged = ColumnChanged.newBuilder()
                        .setAction(ColumnAction.CLEAR)
                        .addAllPositions(columnsToClear.map { it.index })
                        .build()
                }
            }
        }

    override suspend fun resizeFieldTick() =
        mutex.withLock {
            val targetColumnCount = max(activePlayers.size, 2) * 3 + 1
            val (headSuggestion, tailSuggestion) = calculateCollectionResizingSuggestions(
                columns,
                targetColumnCount
            ) { it.isEnabled && (it.hasRows() || it.hasQueue()) }

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
                logger.info("Expanded playing field with new columns: $addedIndices")
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
                logger.info("Reduced playing field with deleted columns: $deletedIndices")
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
                    columns.map { column ->
                        GameStateColumn
                            .newBuilder()
                            .addAllPieces(
                                column.rows.map {
                                    Piece
                                        .newBuilder()
                                        .setOwner(it.owner)
                                        .setFaction(it.faction)
                                        .setScored(it.scored)
                                        .build()
                                })
                            .addAllQueue(
                                column.queue.map {
                                    Piece
                                        .newBuilder()
                                        .setOwner(it.owner)
                                        .setFaction(it.faction)
                                        .setScored(false)
                                        .build()
                                })
                            .setDisabled(column.isDisabled)
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
        logger.info("Emit current event with latest action: ${eventBuilder.actionCase}")
        updates.emit(eventBuilder.build() to state)
    }

    init {
        repeat(initialWidth) { columns += CompleteColumn() }
        runBlocking { emitCurrentState() }
    }
}
