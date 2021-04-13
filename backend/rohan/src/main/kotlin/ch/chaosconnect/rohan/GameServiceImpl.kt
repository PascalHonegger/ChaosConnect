package ch.chaosconnect.rohan

import ch.chaosconnect.api.game.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

@Singleton
class GameServiceImpl : GameService {

    private val columns = Array(7) {
        ArrayList<PieceState?>(6)
    }

    private val updates = MutableSharedFlow<Pair<GameUpdateEvent, GameState>>()

    override suspend fun placePiece(rowIndex: Int, columnIndex: Int) {
        val columnCells: ArrayList<PieceState?>
        try {
            columnCells = columns[columnIndex]
        } catch (exception: IndexOutOfBoundsException) {
            throw createIndexOutOfBoundsException("column", columnIndex)
        }
        if (rowIndex != columnCells.size) {
            throw createIndexOutOfBoundsException("row", rowIndex)
        }
        val state = PieceState
            .newBuilder()
            .setAction(PieceAction.PLACE)
            .setPosition(
                Coordinate
                    .newBuilder()
                    .setRow(rowIndex)
                    .setColumn(columnIndex)
                    .build()
            )
            .build()
        columnCells.add(state)
        emit(
            GameUpdateEvent
                .newBuilder()
                .setColumnChanged(
                    ColumnChanged
                        .newBuilder()
                        .setPosition(columnIndex)
                        .setAction(RowColumnAction.ADD)
                        .build()
                )
                .setRowChanged(
                    RowChanged
                        .newBuilder()
                        .setPosition(rowIndex)
                        .setAction(RowColumnAction.ADD)
                        .build()
                )
                .setPieceChanged(
                    PieceChanged
                        .newBuilder()
                        .addPieces(state)
                        .build()
                )
                .build(),
            GameState
                .newBuilder()
                .addAllColumns(
                    columns.map { column ->
                        GameStateColumn
                            .newBuilder()
                            .addAllPieces(column.map { cell ->
                                Piece
                                    .newBuilder()
                                    .setOwner(cell?.owner)
                                    .setSkin(cell?.skin)
                                    .build()
                            })
                            .build()
                    }
                )
                .build()
        )
    }

    private fun createIndexOutOfBoundsException(indexKind: String, index: Int) =
        IndexOutOfBoundsException("Invalid $indexKind index: $index")

    override fun getGameUpdates(): Flow<Pair<GameUpdateEvent, GameState>> =
        updates

    private suspend fun emit(updateEvent: GameUpdateEvent, state: GameState) =
        updates.emit(updateEvent to state)
}