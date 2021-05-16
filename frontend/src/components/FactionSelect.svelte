<script lang="ts">
    import type { Faction } from "../gen/game_pb";
    import { startPlaying } from "../lib/ChaosConnectClient";
    import { authMetadata } from "../stores/Auth";
    import { playerMap, playersByFaction } from "../stores/GameState";
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
        const teamSize = $playersByFaction.get(faction)?.length ?? 0;
        const otherTeamSize = $playerMap.size - teamSize;
        return teamSize - otherTeamSize >= MAX_TEAM_DIFFERENCE;
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
                {players.length} Player{#if players.length !== 1}s{/if}
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
