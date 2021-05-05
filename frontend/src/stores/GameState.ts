import { derived, Readable, Writable, writable } from "svelte/store";
import { applyUpdate, factions, initialGameState, Player } from "../lib/GameState";
import type { Faction, GameUpdateEvent } from "../gen/game_pb";
import { userIdentifier } from "./Auth";

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

export const playersByFaction = derived(playerMap, $playerMap => {
    const result: Map<Faction, Player[]> = new Map(factions.map(f => [f, []]));
    $playerMap.forEach(player => result.get(player.faction)!.push(player));
    result.forEach(
        players => players.sort((p1, p2) => Number(p2.disconnected) - Number(p1.disconnected))
    );
    return result;
});

export const player: Readable<Player|null> = derived([playerMap, userIdentifier], ([$playerMap, $userIdentifier]) => {
    if ($userIdentifier == null) {
        return null;
    }
    return $playerMap.get($userIdentifier) ?? null;
});
