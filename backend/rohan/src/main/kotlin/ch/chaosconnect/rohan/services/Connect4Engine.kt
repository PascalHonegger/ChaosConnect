package ch.chaosconnect.rohan.services

import ch.chaosconnect.api.game.Faction
import ch.chaosconnect.rohan.model.GameCell
import ch.chaosconnect.rohan.model.GameColumn
import ch.chaosconnect.rohan.model.getPieceOrNull
import java.util.*

data class PieceWithCoordinate(
    val piece: GameCell,
    val column: Int,
    val row: Int
)

/**
 * Top, Right, Down and Left reference the GUI representation.
 * We store the rows within the columns such that 0 is at the bottom.
 * ```
 *  |-----| < Rows-1 / Cols-1
 *  |o    |
 *  |* o *|
 *  |-----|
 *  ^
 * 0/0
 * ```
 */
private enum class Direction(val rowOffset: Int, val colOffset: Int) {
    Top(1, 0),
    TopRight(1, 1),
    Right(0, 1),
    DownRight(-1, 1),
    Down(-1, 0),
    DownLeft(-1, -1),
    Left(0, -1),
    TopLeft(1, -1)
}

private const val piecesToScore = 4

private tailrec fun traverse(
    gameBoard: List<GameColumn>,
    column: Int,
    row: Int,
    direction: Direction,
    faction: Faction,
    steps: Int = 0,
    matchingPieces: MutableList<PieceWithCoordinate> = mutableListOf()
): List<PieceWithCoordinate> {
    val piece = gameBoard.getPieceOrNull(column = column, row = row)

    if (piece == null || piece.scored || piece.faction != faction) {
        return matchingPieces
    }

    val nextColumn = column + direction.colOffset
    val nextRow = row + direction.rowOffset
    matchingPieces.add(
        PieceWithCoordinate(
            piece = piece,
            column = column,
            row = row
        )
    )

    return traverse(
        gameBoard = gameBoard,
        column = nextColumn,
        row = nextRow,
        direction = direction,
        faction = faction,
        steps = steps + 1,
        matchingPieces = matchingPieces
    )
}

fun getWinningPieces(
    gameBoard: List<GameColumn>,
    placedColumn: Int,
    placedRow: Int
): List<PieceWithCoordinate> {
    val placedPiece = gameBoard[placedColumn][placedRow]
    val faction = placedPiece.faction

    val directionToPieces = Direction.values().map {
        it to traverse(
            gameBoard = gameBoard,
            column = placedColumn,
            row = placedRow,
            direction = it,
            faction = faction
        )
    }.toMap(EnumMap(Direction::class.java))

    val allWinningPieces = mutableListOf(
        PieceWithCoordinate(
            placedPiece,
            column = placedColumn,
            row = placedRow
        )
    )

    // Indices opposite to each other
    for ((direction, opposite) in listOf(
        Direction.Top to Direction.Down,
        Direction.TopRight to Direction.DownLeft,
        Direction.Right to Direction.Left,
        Direction.DownRight to Direction.TopLeft
    )) {
        // Pieces for each direction pair
        // Drops the placed piece which is always in the first spot
        val directionPieces =
            directionToPieces[direction] ?: error("Direction not in map")
        val oppositeDirectionPieces =
            directionToPieces[opposite] ?: error("Direction not in map")

        // -1 to account for the placedPiece which is present in both lists
        if ((directionPieces.size + oppositeDirectionPieces.size - 1) >= piecesToScore) {
            allWinningPieces += directionPieces.drop(1)
            allWinningPieces += oppositeDirectionPieces.drop(1)
        }
    }

    return allWinningPieces.takeIf { it.size > 1 } ?: emptyList()
}
