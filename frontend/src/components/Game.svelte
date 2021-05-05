<script lang="ts">
    import type { ClientReadableStream } from "grpc-web";
    import { newEmpty } from "../lib/CommonClient";
    import { gameState, player } from "../stores/GameState";
    import type { GameUpdateEvent } from "../gen/game_pb";
    import type { ChaosConnectServiceClient } from "../gen/JoestarServiceClientPb";
    import { authMetadata } from "../stores/Auth";
    import { onMount } from "svelte";
    import Grid from "./Grid.svelte";
    import PlayerList from "./PlayerList.svelte";
    import FactionSelect from "./FactionSelect.svelte";

    export let client: ChaosConnectServiceClient;

    onMount(() => {
        const updateStream = client.getGameUpdates(
            newEmpty(),
            $authMetadata
        ) as ClientReadableStream<GameUpdateEvent>;
        updateStream.on("data", (updateEvent) => gameState.apply(updateEvent));
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
