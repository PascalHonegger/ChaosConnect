<script lang="ts">
    import type { ClientReadableStream } from "grpc-web";
    import { gameState, playerConnected } from "../stores/GameState";
    import type { GameUpdateEvent } from "../gen/game_pb";
    import { authMetadata } from "../stores/Auth";
    import { onDestroy, onMount } from "svelte";
    import Grid from "./Grid.svelte";
    import PlayerList from "./PlayerList.svelte";
    import FactionSelect from "./FactionSelect.svelte";
    import { getGameUpdates, stopPlaying } from "../lib/ChaosConnectClient";

    const TIMEOUT = 500;

    function applyGameUpdate(gameUpdateEvent: GameUpdateEvent) {
        gameState.apply(gameUpdateEvent);
    }

    let updateStream: ClientReadableStream<GameUpdateEvent>|null = null;

    function delayedRetry() {
        stopGameUpdates();
        setTimeout(startGameUpdates, TIMEOUT);
    }

    function startGameUpdates() {
        gameState.reset();
        updateStream = getGameUpdates($authMetadata);
        updateStream.on('data', applyGameUpdate);
        updateStream.on('error', delayedRetry);
        updateStream.on('end', delayedRetry);
    }

    function stopGameUpdates() {
        if (updateStream != null) {
            updateStream.cancel();
            updateStream.removeListener('data', applyGameUpdate);
            updateStream.removeListener('error', delayedRetry);
            updateStream.removeListener('end', delayedRetry);
        }
    }

    function unloadListener() {
        stopPlaying($authMetadata);
    }

    onMount(() => {
        startGameUpdates();
        window.addEventListener('beforeunload', unloadListener)
    });
    onDestroy(() => {
        stopGameUpdates();
        window.removeEventListener('beforeunload', unloadListener);
    });
</script>

{#if $playerConnected}
    <div class="game">
        <div class="card">
            <PlayerList/>
        </div>

        <div class="card">
            <Grid/>
        </div>
    </div>
{:else}
    <div class="card faction-select">
        <FactionSelect/>
    </div>
{/if}

<style>
    .faction-select {
        margin-left: auto;
        margin-right: auto;
        max-width: 50vw;
        min-width: 250px;
    }

    .game {
        display: grid;
        min-height: 250px;
        column-gap: var(--spacing);
        row-gap: var(--spacing);
    }

    .game > .card {
        display: grid;
    }

    @media only screen and (min-width: 992px) {
        .game {
            grid-template-columns: 2fr 6fr;
        }
    }

    @media only screen and (min-width: 1920px) {
        .game {
            grid-template-columns: 1fr 6fr;
        }
    }
</style>
