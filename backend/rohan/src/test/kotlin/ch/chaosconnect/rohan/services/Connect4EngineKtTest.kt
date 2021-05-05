package ch.chaosconnect.rohan.services

import ch.chaosconnect.api.game.Faction
import ch.chaosconnect.rohan.model.GameCell
import ch.chaosconnect.rohan.model.GameColumn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

fun mockPiece(
    column: Int,
    row: Int,
    faction: Faction,
    scored: Boolean = false
) = PieceWithCoordinate(GameCell("Some-Owner", faction, scored), column, row)

/**
 * ```
 *  +--------------------+
 *  |   r r    r r r   y |
 *  +--------------------+
 * ```
 */
val horizontalScoreBoard = listOf(
    mockPiece(1, 0, Faction.RED),
    mockPiece(2, 0, Faction.RED),
    mockPiece(4, 0, Faction.RED),
    mockPiece(5, 0, Faction.RED),
    mockPiece(6, 0, Faction.RED),
    mockPiece(8, 0, Faction.YELLOW),
)

/**
 * ```
 *  +------+
 *  |      |
 *  | r    |
 *  | r  y |
 *  | r  y |
 *  +------+
 * ```
 */
val verticalScoreBoard = listOf(
    mockPiece(0, 0, Faction.RED),
    mockPiece(0, 1, Faction.RED),
    mockPiece(0, 2, Faction.RED),
    mockPiece(1, 0, Faction.YELLOW),
    mockPiece(1, 1, Faction.YELLOW),
)

/**
 * ```
 *  +-------------+
 *  |             |
 *  | y y r   r y |
 *  | y r y r r r |
 *  | y r r y y r |
 *  +-------------+
 * ```
 */
val diagonalScoreBoard = listOf(
    mockPiece(0, 0, Faction.YELLOW),
    mockPiece(0, 1, Faction.YELLOW),
    mockPiece(0, 2, Faction.YELLOW),
    mockPiece(1, 0, Faction.RED),
    mockPiece(1, 1, Faction.RED),
    mockPiece(1, 2, Faction.YELLOW),
    mockPiece(2, 0, Faction.RED),
    mockPiece(2, 1, Faction.YELLOW),
    mockPiece(2, 2, Faction.RED),
    mockPiece(3, 0, Faction.YELLOW),
    mockPiece(3, 1, Faction.RED),
    mockPiece(4, 0, Faction.YELLOW),
    mockPiece(4, 1, Faction.RED),
    mockPiece(4, 2, Faction.RED),
    mockPiece(5, 0, Faction.RED),
    mockPiece(5, 1, Faction.RED),
    mockPiece(5, 2, Faction.YELLOW),
)

fun List<PieceWithCoordinate>.asGameBoard(): List<GameColumn> {
    val columns: List<GameColumn> = (0..20).map { mutableListOf() }
    for ((piece, column) in this) {
        columns[column].add(piece)
    }
    return columns
}

internal class Connect4EngineKtTest {
    @ParameterizedTest
    @MethodSource("losingMoves")
    fun `does not identify pieces if not connected`(
        placedPiece: PieceWithCoordinate,
        gameBoard: List<PieceWithCoordinate>
    ) {
        val board = (gameBoard + placedPiece).asGameBoard()
        val winningPieces = getWinningPieces(
            gameBoard = board,
            placedColumn = placedPiece.column,
            placedRow = placedPiece.row
        )
        assertTrue(winningPieces.isEmpty()) { "Expected no winning pieces, got ${winningPieces.toList()}" }
    }

    @ParameterizedTest
    @MethodSource("winningMoves")
    fun `does identify all pieces if at least 4 are connected`(
        placedPiece: PieceWithCoordinate,
        gameBoard: List<PieceWithCoordinate>,
        expectedPieces: List<PieceWithCoordinate>
    ) {
        val board = (gameBoard + placedPiece).asGameBoard()
        val winningPieces = getWinningPieces(
            gameBoard = board,
            placedColumn = placedPiece.column,
            placedRow = placedPiece.row
        )
        assertEquals(
            expectedPieces.sortedWith(compareBy({ it.column }, { it.row })),
            winningPieces.sortedWith(compareBy({ it.column }, { it.row }))
        )
    }

    companion object {
        @JvmStatic
        private fun winningMoves() =
            Stream.of(
                Arguments.of(
                    mockPiece(3, 0, Faction.RED),
                    horizontalScoreBoard,
                    (1..6).map { mockPiece(it, 0, Faction.RED) }
                ),
                Arguments.of(
                    mockPiece(7, 0, Faction.RED),
                    horizontalScoreBoard,
                    (4..7).map { mockPiece(it, 0, Faction.RED) }
                ),
                Arguments.of(
                    mockPiece(0, 3, Faction.RED),
                    verticalScoreBoard,
                    (0..3).map { mockPiece(0, it, Faction.RED) }
                ),
                Arguments.of(
                    mockPiece(0, 3, Faction.YELLOW),
                    diagonalScoreBoard,
                    listOf(
                        mockPiece(0, 3, Faction.YELLOW),
                        mockPiece(0, 2, Faction.YELLOW),
                        mockPiece(0, 1, Faction.YELLOW),
                        mockPiece(0, 0, Faction.YELLOW),
                        mockPiece(1, 2, Faction.YELLOW),
                        mockPiece(2, 1, Faction.YELLOW),
                        mockPiece(3, 0, Faction.YELLOW),
                    )
                ),
                Arguments.of(
                    mockPiece(5, 3, Faction.RED),
                    diagonalScoreBoard,
                    listOf(
                        mockPiece(2, 0, Faction.RED),
                        mockPiece(3, 1, Faction.RED),
                        mockPiece(4, 2, Faction.RED),
                        mockPiece(5, 3, Faction.RED),
                    )
                ),
            )

        @JvmStatic
        private fun losingMoves() =
            Stream.of(
                Arguments.of(
                    mockPiece(3, 0, Faction.YELLOW),
                    horizontalScoreBoard
                ),
                Arguments.of(
                    mockPiece(0, 0, Faction.RED),
                    horizontalScoreBoard
                ),
                Arguments.of(
                    mockPiece(0, 0, Faction.RED),
                    horizontalScoreBoard
                ),
                Arguments.of(
                    mockPiece(0, 3, Faction.YELLOW),
                    verticalScoreBoard
                ),
                Arguments.of(
                    mockPiece(1, 2, Faction.YELLOW),
                    verticalScoreBoard
                ),
                Arguments.of(
                    mockPiece(0, 3, Faction.RED),
                    diagonalScoreBoard
                ),
                Arguments.of(
                    mockPiece(5, 3, Faction.YELLOW),
                    diagonalScoreBoard
                ),
                Arguments.of(
                    mockPiece(3, 2, Faction.RED),
                    diagonalScoreBoard
                ),
            )
    }
}
