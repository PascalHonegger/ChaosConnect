import { derived, Writable, writable } from "svelte/store";
import { applyUpdate, initialGameState, Player } from "../lib/GameState";
import { Faction, GameUpdateEvent } from "../gen/game_pb";

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

export const factions = derived(playerMap, $playerMap => {
    const factions: Map<Faction, Player[]> = new Map([
        [Faction.RED, []],
        [Faction.YELLOW, []]
    ]);
    $playerMap.forEach(player => factions.get(player.faction)?.push(player));
    factions.forEach(players => 
        players.sort((p1, p2) => Number(p2.disconnected) - Number(p1.disconnected))
    );
    return factions;
});

export const player: Writable<Player> = writable({
    identifier: 'Player1',
    displayName: 'JoJo',
    disconnected: false,
    faction: Faction.RED
});
