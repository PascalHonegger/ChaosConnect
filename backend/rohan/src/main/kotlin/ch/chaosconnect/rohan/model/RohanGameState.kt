package ch.chaosconnect.rohan.model

import ch.chaosconnect.api.game.Faction
import ch.chaosconnect.api.game.PieceAction
import ch.chaosconnect.api.game.PieceState
import ch.chaosconnect.api.game.QueueState
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

fun List<GameColumn>.getPieceOrNull(column: Int, row: Int) =
    getOrNull(column)?.getOrNull(row)

typealias GameColumn = MutableList<GameCell>
typealias GameColumnQueue = Queue<QueueCell>
