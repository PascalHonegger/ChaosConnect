import {
    ColumnAction,
    ColumnChanged,
    Faction,
    GameState as ApiGameState,
    GameStateColumn,
    GameUpdateEvent,
    Piece as ApiPiece,
    PieceAction,
    PieceChanged,
    PieceState,
    PlayerAction,
    PlayerChanged,
    PlayerState,
    QueueChanged,
    QueueState
} from "../gen/game_pb";


export const factions = Object.freeze([Faction.RED, Faction.YELLOW]);

export interface Player {
    identifier: string;
    displayName: string;
    faction: Faction;
    score: number;
    disconnected: boolean;
}

function newPlayer(identifier: string, playerState: PlayerState): Player {
    return {
        identifier,
        displayName: playerState.getDisplayName(),
        faction: playerState.getFaction(),
        score: playerState.getScore(),
        disconnected: false
    };
}

function disconnectedPlayer(player: Player): Player
{
    return {
        ...player,
        disconnected: true
    };
} 

export interface Piece {
    faction: Faction;
    owner: string;
}

function newPiece(piece: ApiPiece | PieceState | QueueState): Piece {
    return {
        owner: piece.getOwner(),
        faction: piece.getFaction()
    };
}

export interface Cell {
    disabled: boolean;
    scored: boolean;
    piece: Piece | null;
}

function newCell(piece?: ApiPiece | PieceState): Cell {
    return {
        disabled: false,
        scored: piece?.getScored() ?? false,
        piece: piece != null ? newPiece(piece) : null
    };
}

function disabledCell(cell: Cell): Cell {
    return {
        ...cell,
        disabled: true
    }
}

function clearedCell(): Cell {
    return {
        disabled: false,
        scored: false,
        piece: null
    }
}

function piecePlacedCell(cell: Cell, piece: ApiPiece | PieceState): Cell {
    return {
        ...cell,
        piece: newPiece(piece)
    };
}

function pieceScoredCell(cell: Cell): Cell {
    return {
        ...cell,
        scored: true
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
    const computedColumn: Column = {cells, queue};
    return column?.getDisabled() ? disabledColumn(computedColumn) : computedColumn;
}

function disabledColumn(column: Column): Column {
    return {
        ...column,
        cells: column.cells.map(disabledCell),
    };
}

function clearedColumn(column: Column): Column {
    return {
        queue: [],
        cells: column.cells.map(clearedCell)
    }
}

function piecePlacedColumn(column: Column, piece: PieceState): Column {
    const cells = [...column.cells];
    const index = cells.findIndex(c => c.piece == null);
    cells[index] = piecePlacedCell(cells[index], piece);
    return {
        cells: cells,
        queue: column.queue.slice(1)
    };
}

function pieceScoredColumn(column: Column, row: number): Column {
    const cells = [...column.cells];
    cells[row] = pieceScoredCell(cells[row]);
    return {
        cells: cells,
        queue: column.queue
    };
}

function pieceEnqueuedColumn(column: Column, piece: QueueState): Column {
    return {
        ...column,
        queue: [...column.queue, newPiece(piece)]
    };
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
    const positions = columnChanged.getPositionsList();
    const {numberOfRows, columns} = gameState;
    let newColumns: Column[];
    switch (columnChanged.getAction()) {
        case ColumnAction.ADD:
            newColumns = [...columns];
            for (const position of positions) {
                newColumns.splice(position, 0, newColumn(numberOfRows));
            }
            return {
                ...gameState,
                columns: newColumns
            }
        case ColumnAction.DISABLE:
            newColumns = [...columns];
            for (const position of positions) {
                newColumns[position] = disabledColumn(newColumns[position]);
            }
            return {
                ...gameState,
                columns: newColumns
            }
        case ColumnAction.DELETE:
            return {
                ...gameState,
                columns: columns.filter((_, index) => !positions.includes(index))
            }
        case ColumnAction.CLEAR:
            newColumns = [...columns];
            for (const position of positions) {
                newColumns[position] = clearedColumn(newColumns[position]);
            }
            return {
                ...gameState,
                columns: newColumns
            }
    }
}

function handlePieceChanged(gameState: GameState, pieceChanged: PieceChanged): GameState {
    const columns = [...gameState.columns];
    for (const piece of pieceChanged.getPiecesList()) {
        switch (piece.getAction()) {
            case PieceAction.PLACE:
                columns[piece.getColumn()] = piecePlacedColumn(columns[piece.getColumn()], piece);
                break;
            case PieceAction.SCORE:
                columns[piece.getColumn()] = pieceScoredColumn(columns[piece.getColumn()], piece.getRow());
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
        const columnIndex = piece.getColumn();
        columns[columnIndex] = pieceEnqueuedColumn(columns[columnIndex], piece);
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
        case PlayerAction.JOIN:
        case PlayerAction.UPDATE:
            if (playerState == null) {
                console.warn('PlayerAction JOINED with null playerState');
                break;
            }
            playerMap.set(identifier, newPlayer(identifier, playerState));
            break;
        case PlayerAction.DISCONNECT:
            const player = playerMap.get(identifier);
            if (player != null) {
                playerMap.set(identifier, disconnectedPlayer(player));
            }
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
        case GameUpdateEvent.ActionCase.PLAYER_CHANGED:
            return handlePlayerChanged(currentState, updateEvent.getPlayerChanged()!);
        default:
            console.warn('Unknown GameUpdateEvent ActionCase');
            return currentState;
    }
}
