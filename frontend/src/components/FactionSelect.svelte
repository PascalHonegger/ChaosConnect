<script lang="ts">
    import type { Faction } from "../gen/game_pb";
    import { startPlaying } from "../lib/ChaosConnectClient";
import type { Player } from "../lib/GameState";
    import { authMetadata } from "../stores/Auth";
    import { playersByFaction } from "../stores/GameState";
    import Piece from "./Piece.svelte";
    import Spinner from "./Spinner.svelte";

    const MAX_TEAM_DIFFERENCE = 2;

    let waiting = false;
    let errorOccurred = false;

    async function chooseFaction(faction: Faction) {
        if (waiting) {
            return;
        }
        waiting = true;
        try {
            await startPlaying(faction, $authMetadata);
        } catch (e) {
            errorOccurred = true;
            waiting = false;
            console.error(e);
        }
    }

    function isUnbalanced(faction: Faction): boolean {
        let teamSize = 0;
        let otherTeamSize = 0;
        for (const [key, players] of $playersByFaction) {
            const count = countConnected(players);
            if (key === faction) {
                teamSize = count;
            } else {
                otherTeamSize = count;
            }
        }
        return teamSize - otherTeamSize >= MAX_TEAM_DIFFERENCE;
    }

    function countConnected(players: Player[]) {
        let count = 0;
        for (const p of players) {
            if (!p.disconnected) {
                count++;
            }
        }
        return count;
    }
</script>

<div class="faction-select">
    {#if errorOccurred}
        <p>An error occurred</p>
    {/if}
    {#if waiting}
        <Spinner />
    {:else}
        <h2>Choose Your Faction</h2>
        {#each [...$playersByFaction] as [faction, players]}
            <button disabled={isUnbalanced(faction)} on:click={() => chooseFaction(faction)}>
                <Piece {faction} />
                {countConnected(players)} Player{#if countConnected(players) !== 1}s{/if}
            </button>
        {/each}
    {/if}
</div>

<style>
    .faction-select {
        text-align: center;
        align-self: center;
    }
</style>
