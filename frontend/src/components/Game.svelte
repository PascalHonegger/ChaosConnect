<script lang="ts">
    import type { ClientReadableStream } from "grpc-web";
    import { gameState, player } from "../stores/GameState";
    import type { GameUpdateEvent } from "../gen/game_pb";
    import { authMetadata } from "../stores/Auth";
    import { onDestroy, onMount } from "svelte";
    import Grid from "./Grid.svelte";
    import PlayerList from "./PlayerList.svelte";
    import FactionSelect from "./FactionSelect.svelte";
    import { getGameUpdates } from "../lib/ChaosConnectClient";

    function applyGameUpdate(gameUpdateEvent: GameUpdateEvent) {
        gameState.apply(gameUpdateEvent);
    }

    let updateStream: ClientReadableStream<GameUpdateEvent>;
    onMount(() => {
        gameState.reset();
        updateStream = getGameUpdates($authMetadata);
        updateStream.on('data', applyGameUpdate);
    });
    onDestroy(() => {
        updateStream.cancel();
        updateStream.removeListener('data', applyGameUpdate);
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
