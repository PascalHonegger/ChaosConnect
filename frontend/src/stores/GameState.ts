import { derived, writable } from "svelte/store";
import { applyUpdate, initialGameState } from "../lib/GameState";
import type { GameUpdateEvent } from "../gen/game_pb";

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

export const columns = derived(gameState, $gameState => $gameState.columns);

export const playerMap = derived(gameState, $gameState => $gameState.playerMap);
