import { derived, Readable, writable } from "svelte/store";
import { applyUpdate, Column, initialGameState } from "./GameState";
import type { GameUpdateEvent } from "./gen/game_pb";



function createGameStateStore() {
    const { subscribe, update } = writable(initialGameState());

    return {
        subscribe,
        apply(updateEvent: GameUpdateEvent) {
            update(currentState => applyUpdate(currentState, updateEvent));
        }
    }
}

export const gameState = createGameStateStore();

export const columns: Readable<Column[]> = derived(gameState, $gameState => $gameState.columns);
