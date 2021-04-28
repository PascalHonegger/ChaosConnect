import {
    ColumnChanged,
    GameState as ApiGameState,
    GameStateColumn,
    GameUpdateEvent,
    Piece as ApiPiece,
    RowChanged,
    RowColumnAction,
    Faction,
    PlayerState,
    PieceChanged,
    PieceAction,
    PieceState,
    QueueChanged,
    QueueState,
    PlayerChanged,
    PlayerAction
} from "./gen/game_pb";


export interface Player {
    identifier: string;
    displayName: string;
}

function newPlayer(identifier: string, playerState: PlayerState): Player {
    return {
        identifier,
        displayName: playerState.getDisplayName(),
    };
}

export interface Piece {
    faction: Faction;
    owner: string;
}

function newPiece(piece: ApiPiece | PieceState): Piece {
    const skin = piece.getSkin();
    if (skin == null) {
        throw new Error('Can not get faction of piece without skin field');
    }
    return {
        owner: piece.getOwner(),
        faction: skin.getFaction()
    };
}

function newQueuePiece(queueState: QueueState): Piece {
    return {
        owner: queueState.getOwner(),
        faction: queueState.getFaction()
    };
}

export interface Cell {
    disabled: boolean;
    piece: Piece | null;
}

function newCell(piece?: ApiPiece | PieceState): Cell {
    return {
        disabled: false,
        piece: piece != null ? newPiece(piece) : null
    };
}

function disabledCell(cell: Cell): Cell {
    return {
        ...cell,
        disabled: true
    }
}

function clearedCell(cell: Cell): Cell {
    return {
        ...cell,
        piece: null
    }
}

function piecePlacedCell(cell: Cell, piece: ApiPiece | PieceState): Cell {
    return {
        ...cell,
        piece: newPiece(piece)
    };
}

export interface Column {
    queue: Piece[];
    cells: Cell[];
}

function newColumn(numberOfRows: number, column?: GameStateColumn): Column {
    const cells = column?.getPiecesList().map(piece => newCell(piece)) ?? [];
    while (cells.length < numberOfRows) {
        cells.push(newCell());
    }
    const queue = column?.getQueueList().map(piece => newPiece(piece)) ?? [];
    return { cells: cells, queue };
}

function disabledColumn(column: Column): Column {
    return {
        ...column,
        cells: column.cells.map(disabledCell),
    };
}

function clearedColumn(column: Column): Column {
    return {
        ...column,
        cells: column.cells.map(clearedCell)
    }
}

function piecePlacedColumn(column: Column, piece: PieceState, row: number): Column {
    const cells = [...column.cells];
    cells[row] = piecePlacedCell(cells[row], piece);
    return {
        cells: cells,
        queue: column.queue.slice(1)
    };
}

function pieceEnqueuedColumn(column: Column, piece: QueueState): Column {
    return {
        ...column,
        queue: [...column.queue, newQueuePiece(piece)]
    };
}

function rowChangeAppliedColumn(column: Column, rowChanged: RowChanged): Column {
    const { cells } = column;
    const position = rowChanged.getPosition();
    let newCells: Cell[];
    switch (rowChanged.getAction()) {
        case RowColumnAction.ADD:
            return {
                ...column,
                cells: [
                    ...cells.slice(0, position),
                    newCell(),
                    ...cells.slice(position)
                ]
            };
        case RowColumnAction.DISABLE:
            newCells = [...cells];
            newCells[position] = disabledCell(newCells[position]);
            return {
                ...column,
                cells: newCells
            };
        case RowColumnAction.DELETE:
            return {
                ...column,
                cells: cells.filter((_, index) => index !== position)
            }
        case RowColumnAction.CLEAR: {
            newCells = [...cells];
            newCells[position] = clearedCell(newCells[position]);
            return {
                ...column,
                cells: newCells
            };
        }
    }
}

export interface GameState {
    numberOfRows: number;
    columns: Column[];
    playerMap: Map<string, Player>;
}

export function initialGameState(): GameState {
    return {
        numberOfRows: 0,
        columns: [],
        playerMap: new Map()
    };
}

function newGameState(gameState: ApiGameState): GameState {
    gameState.getPlayersMap();
    const numberOfRows = gameState.getNumberOfRows();
    const columns = gameState.getColumnsList().map(column => newColumn(numberOfRows, column));
    const playerMap = new Map<string, Player>();
    gameState.getPlayersMap().forEach(
        (player, identifier) => playerMap.set(identifier, newPlayer(identifier, player))
    );
    return {
        numberOfRows,
        columns,
        playerMap
    };
}

function handleColumnChanged(gameState: GameState, columnChanged: ColumnChanged): GameState {
    const position = columnChanged.getPosition();
    const { numberOfRows, columns } = gameState;
    let newColumns: Column[];
    switch (columnChanged.getAction()) {
        case RowColumnAction.ADD:
            return {
                ...gameState,
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
                ...gameState,
                columns: newColumns
            }
        case RowColumnAction.DELETE:
            return {
                ...gameState,
                columns: columns.filter((_, index) => index !== position)
            }
        case RowColumnAction.CLEAR:
            newColumns = [...columns];
            newColumns[position] = clearedColumn(newColumns[position]);
            return {
                ...gameState,
                columns: newColumns
            }
    }
}

function handleRowChanged(gameState: GameState, rowChanged: RowChanged): GameState {
    let { numberOfRows } = gameState;
    switch (rowChanged.getAction()) {
        case RowColumnAction.ADD:
            ++numberOfRows;
            break;
        case RowColumnAction.DELETE:
            --numberOfRows;
            break;
    }
    return {
        ...gameState,
        numberOfRows,
        columns: gameState.columns.map(column => rowChangeAppliedColumn(column, rowChanged))
    }
}

function handlePieceChanged(gameState: GameState, pieceChanged: PieceChanged): GameState {
    const columns = [...gameState.columns];
    for (const piece of pieceChanged.getPiecesList()) {
        switch (piece.getAction()) {
            case PieceAction.PLACE:
                const position = piece.getPosition();
                if (position == null) {
                    console.warn('PiecePlaced has no position')
                    continue;
                }
                columns[position.getColumn()] = piecePlacedColumn(columns[position.getColumn()], piece, position.getRow());
                break;
            default:
                console.warn('Unknown PieceAction');
        }
    }
    return {
        ...gameState,
        columns
    };
}

function handleQueueChanged(gameState: GameState, queueChanged: QueueChanged): GameState {
    const columns = [...gameState.columns];
    for (const piece of queueChanged.getPiecesList()) {
        const position = piece.getPosition();
        if (position == null) {
            console.warn('QueueChanged has no position');
            continue;
        }
        columns[position.getColumn()] = pieceEnqueuedColumn(columns[position.getColumn()], piece);
    }
    return {
        ...gameState,
        columns
    }
}

function handlePlayerChanged(gameState: GameState, playerChanged: PlayerChanged): GameState {
    const playerMap = new Map(gameState.playerMap);
    const identifier = playerChanged.getPlayer();
    const playerState = playerChanged.getState();
    switch (playerChanged.getAction()) {
        case PlayerAction.JOINED:
        case PlayerAction.UPDATED:
            if (playerState == null) {
                console.warn('PlayerAction JOINED with null playerState');
                break;
            }
            playerMap.set(identifier, newPlayer(identifier, playerState));
            break;
        case PlayerAction.DISCONNECTED:
            playerMap.delete(identifier);
            break;
        default:
            console.warn('Unknown PlayerChanged Action');
    }
    return {
        ...gameState,
        playerMap
    }
}

export function applyUpdate(currentState: GameState, updateEvent: GameUpdateEvent): GameState {
    switch (updateEvent.getActionCase()) {
        case GameUpdateEvent.ActionCase.GAME_STATE:
            return newGameState(updateEvent.getGameState()!);
        case GameUpdateEvent.ActionCase.PIECE_CHANGED:
            return handlePieceChanged(currentState, updateEvent.getPieceChanged()!);
        case GameUpdateEvent.ActionCase.QUEUE_CHANGED:
            return handleQueueChanged(currentState, updateEvent.getQueueChanged()!);
        case GameUpdateEvent.ActionCase.COLUMN_CHANGED:
            return handleColumnChanged(currentState, updateEvent.getColumnChanged()!);
        case GameUpdateEvent.ActionCase.ROW_CHANGED:
            return handleRowChanged(currentState, updateEvent.getRowChanged()!)
        case GameUpdateEvent.ActionCase.PLAYER_CHANGED:
            return handlePlayerChanged(currentState, updateEvent.getPlayerChanged()!);
        default:
            console.warn('Unknown GameUpdateEvent ActionCase');
            return currentState;
    }
}
