<script lang="ts">
    import type { Faction } from "../gen/game_pb";
    import { startPlaying } from "../lib/ChaosConnectClient";
    import { factions } from "../lib/GameState";
    import { authMetadata } from "../stores/Auth";
    import Piece from "./Piece.svelte";
    import Spinner from "./Spinner.svelte";

    let waiting = false;
    let errorOccured = false;

    async function chooseFaction(faction: Faction) {
        if (waiting) {
            return;
        }
        waiting = true;
        try {
            await startPlaying(faction, $authMetadata);
        } catch (e) {
            errorOccured = true;
            waiting = false;
            console.error(e);
        }
    }
</script>

<div class="card">
    {#if errorOccured}
        <p>An error occurred</p>
    {/if}
    {#if waiting}
        <Spinner />
    {:else}
        <h2>Choose faction:</h2>
        {#each factions as faction}
            <button on:click={() => chooseFaction(faction)}>
                <Piece {faction} />
            </button>
        {/each}
    {/if}
</div>

<style>
    .card {
        padding: 1rem;
        text-align: center;
        align-self: center;
    }
</style>
