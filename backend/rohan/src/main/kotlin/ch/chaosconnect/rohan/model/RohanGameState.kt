package ch.chaosconnect.rohan.model

import ch.chaosconnect.api.game.Faction
import ch.chaosconnect.api.game.PieceAction
import ch.chaosconnect.api.game.PieceState
import ch.chaosconnect.api.game.QueueState
import java.time.LocalDateTime
import java.util.*

data class QueueCell(val owner: String, val faction: Faction) {
    fun toQueueState(columnIndex: Int): QueueState = QueueState
        .newBuilder()
        .setColumn(columnIndex)
        .setFaction(faction)
        .setOwner(owner)
        .build()
}

data class GameCell(
    val owner: String,
    val faction: Faction,
    val scored: Boolean
) {
    fun toPieceState(
        action: PieceAction,
        columnIndex: Int,
        rowIndex: Int
    ): PieceState = PieceState
        .newBuilder()
        .setAction(action)
        .setFaction(faction)
        .setOwner(owner)
        .setScored(scored)
        .setColumn(columnIndex)
        .setRow(rowIndex)
        .build()
}

data class CompleteColumn(
    val rows: GameColumn = mutableListOf(),
    val queue: GameColumnQueue = LinkedList(),
    var disabledAt: LocalDateTime? = null
) {
    val isEnabled get() = !isDisabled
    val isDisabled get() = disabledAt != null
    val numRows get() = rows.size
    fun hasRows() = rows.isNotEmpty()
    fun hasQueue() = queue.isNotEmpty()
    fun enqueue(element: QueueCell) = queue.add(element)
    fun dequeue(): QueueCell = queue.poll()
    fun place(element: GameCell) = rows.add(element)
    fun reset() {
        rows.clear()
        queue.clear()
        disabledAt = null
    }
}

fun List<CompleteColumn>.onlyRows() = map { it.rows }

fun List<GameColumn>.getPieceOrNull(column: Int, row: Int) =
    getOrNull(column)?.getOrNull(row)

typealias GameColumn = MutableList<GameCell>
typealias GameColumnQueue = Queue<QueueCell>
