<script lang="ts">
    import twemoji from "../lib/Twemoji";
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
        <div class="wrapper">
            {#each [...$playersByFaction] as [faction, players], i}
                {#if i !== 0}
                    <span use:twemoji class="vs-button">🆚</span>
                {/if}
                <button
                    disabled={isUnbalanced(faction)}
                    on:click={() => chooseFaction(faction)}
                >
                    <span class="piece-wrapper">
                        <Piece {faction} />
                    </span>
                    <span>
                        {countConnected(players)} Player{#if countConnected(players) !== 1}s{/if}
                    </span>
                </button>
            {/each}
        </div>
    {/if}
</div>

<style>
    .faction-select {
        text-align: center;
    }

    .wrapper {
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
    }

    .vs-button {
        font-size: 2em;
        display: flex;
        margin: 0 var(--spacing);
    }

    .piece-wrapper {
        position: relative;
        display: block;
        width: var(--piece-size);
        height: var(--piece-size);
        margin-left: auto;
        margin-right: auto;
    }

    button {
        margin: 0;
    }
</style>
