import { ColumnChanged, GameState, GameStateColumn, GameUpdateEvent, RowChanged, RowColumnAction } from "./gen/game_pb";

export enum Faction {
    RED = 'red',
    YELLOW = 'yellow'
}

export interface Piece {

}

function newPiece(): Piece {
    return {};
}

export interface Slot {
    disabled: boolean;
    piece: Piece | null;
}

function newSlot(): Slot {
    return {
        disabled: false,
        piece: null
    };
}

function disabledSlot({ piece }: Slot): Slot {
    return {
        disabled: true,
        piece
    }
}

function clearedSlot({ disabled }: Slot): Slot {
    return {
        disabled,
        piece: null
    }
}

export interface Column {
    slots: Slot[];
}

function newColumn(numberOfRows: number, column?: GameStateColumn): Column {
    const slots: Slot[] = [];
    while (slots.length < numberOfRows) {
        slots.push(newSlot());
    }
    column?.getPiecesList().forEach((_, index) => slots[index].piece = newPiece());
    return { slots };
}

function disabledColumn({ slots }: Column): Column {
    return {
        slots: slots.map(disabledSlot)
    };
}

function clearedColumn({ slots }: Column): Column {
    return {
        slots: slots.map(clearedSlot)
    }
}

function rowChangeAppliedColumn({ slots }: Column, rowChanged: RowChanged): Column {
    const position = rowChanged.getPosition();
    let newSlots;
    switch (rowChanged.getAction()) {
        case RowColumnAction.ADD:
            return {
                slots: [
                    ...slots.slice(0, position),
                    newSlot(),
                    ...slots.slice(position)
                ]
            };
        case RowColumnAction.DISABLE: 
            newSlots = [...slots];
            newSlots[position] = disabledSlot(newSlots[position]);
            return {
                slots: newSlots
            };
        case RowColumnAction.DELETE:
            return {
                slots: slots.filter((_, index) => index !== position)
            }
        case RowColumnAction.CLEAR: {
            newSlots = [...slots];
            newSlots[position] = clearedSlot(newSlots[position]);
            return {
                slots: newSlots
            };
        }
    }
}

export interface LocalGameState {
    numberOfRows: number;
    columns: Column[];
}

export function initialGameState(): LocalGameState {
    return {
        numberOfRows: 0,
        columns: []
    };
}

function newGameState(gameState: GameState): LocalGameState {
    const numberOfRows = gameState.getNumberOfRows();
    const columns = gameState.getColumnsList().map(column => newColumn(numberOfRows, column));
    return {
        numberOfRows,
        columns
    };
}

function handleColumnChanged({ columns, numberOfRows }: LocalGameState, columnChanged: ColumnChanged): LocalGameState {
    const position = columnChanged.getPosition();
    let newColumns;
    switch (columnChanged.getAction()) {
        case RowColumnAction.ADD:
            return {
                numberOfRows,
                columns: [
                    ...columns.slice(0, position),
                    newColumn(numberOfRows),
                    ...columns.slice(position)
                ]
            }
        case RowColumnAction.DISABLE:
            newColumns = [...columns];
            newColumns[position] = disabledColumn(newColumns[position]); 
            return {
                numberOfRows,
                columns: newColumns
            }
        case RowColumnAction.DELETE:
            return {
                numberOfRows,
                columns: columns.filter((_, index) => index !== position)
            }
        case RowColumnAction.CLEAR:
            newColumns = [...columns]; 
            newColumns[position] = clearedColumn(newColumns[position]);
            return {
                numberOfRows,
                columns: newColumns
            }
    }
}

function handleRowChanged({ columns, numberOfRows }: LocalGameState, rowChanged: RowChanged): LocalGameState {
    switch (rowChanged.getAction()) {
        case RowColumnAction.ADD:
            ++numberOfRows;
            break;
        case RowColumnAction.DELETE:
            --numberOfRows;
            break;
    }
    return {
        numberOfRows,
        columns: columns.map(column => rowChangeAppliedColumn(column, rowChanged))
    }
}

export function applyUpdate(currentState: LocalGameState, updateEvent: GameUpdateEvent): LocalGameState {
    if (updateEvent.hasGameState()) {
        return newGameState(updateEvent.getGameState()!);
    }
    if (updateEvent.hasColumnChanged()) {
        return handleColumnChanged(currentState, updateEvent.getColumnChanged()!);
    }
    if (updateEvent.hasRowChanged()) {
        return handleRowChanged(currentState, updateEvent.getRowChanged()!);
    }
    return currentState;
}
