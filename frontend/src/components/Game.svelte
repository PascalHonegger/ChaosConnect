<script lang="ts">
    import type { ClientReadableStream } from "grpc-web";
    import { newEmpty } from "../lib/CommonClient";
    import { gameState, player } from "../stores/GameState";
    import type { GameUpdateEvent } from "../gen/game_pb";
    import type { ChaosConnectServiceClient } from "../gen/JoestarServiceClientPb";
    import { authMetadata } from "../stores/Auth";
    import { onDestroy, onMount } from "svelte";
    import Grid from "./Grid.svelte";
    import PlayerList from "./PlayerList.svelte";
    import FactionSelect from "./FactionSelect.svelte";

    export let client: ChaosConnectServiceClient;

    function applyGameUpdate(gameUpdateEvent: GameUpdateEvent) {
        gameState.apply(gameUpdateEvent);
    }

    let updateStream: ClientReadableStream<GameUpdateEvent>;
    onMount(() => {
        gameState.reset();
        updateStream = client.getGameUpdates(
            newEmpty(),
            $authMetadata
        ) as ClientReadableStream<GameUpdateEvent>;
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
