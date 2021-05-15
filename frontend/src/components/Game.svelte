<script lang="ts">
    import type { ClientReadableStream } from "grpc-web";
    import { gameState, player } from "../stores/GameState";
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

{#if $player}
    <div class="game">
        <PlayerList />

        <Grid />
    </div>
{:else}
    <FactionSelect />
{/if}

<style>
    .game {
        display: grid;
        grid-template-columns: 1fr 6fr;
    }
</style>
