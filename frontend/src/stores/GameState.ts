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
        },
        reset() {
            update(initialGameState);
        }
    }
}

export const gameState = createGameStateStore();

export const columns = derived(gameState, $gameState => $gameState.columns);

export const playerMap = derived(gameState, $gameState => $gameState.playerMap);

function comparePlayers(p1: Player, p2: Player): number {
    if (p1.disconnected === p2.disconnected) {
        return p2.score - p1.score;
    }
    return p1.disconnected ? 1 : -1;
}

export const playersByFaction = derived(playerMap, $playerMap => {
    const result: Map<Faction, Player[]> = new Map(factions.map(f => [f, []]));
    $playerMap.forEach(player => result.get(player.faction)!.push(player));
    result.forEach(players => players.sort(comparePlayers));
    return result;
});

export const player: Readable<Player|null> = derived([playerMap, userIdentifier], ([$playerMap, $userIdentifier]) => {
    if ($userIdentifier == null) {
        return null;
    }
    return $playerMap.get($userIdentifier) ?? null;
});

export const playerConnected: Readable<boolean> = derived(player, ($player) =>
    $player != null && !$player.disconnected
);
