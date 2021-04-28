<script lang="ts">
    import type { ClientReadableStream } from "grpc-web";
    import Column from "./Column.svelte";
    import { newEmpty } from "../lib/CommonClient";
    import { columns, gameState } from "../stores/GameState";
    import type { GameUpdateEvent } from "../gen/game_pb";
    import type { ChaosConnectServiceClient } from "../gen/JoestarServiceClientPb";
    import { authMetadata } from "../stores/Auth";

    export let client: ChaosConnectServiceClient;

    const updateStream = client.getGameUpdates(
        newEmpty(),
        $authMetadata
    ) as ClientReadableStream<GameUpdateEvent>;
    updateStream.on("data", (updateEvent) => gameState.apply(updateEvent));
</script>

<div class="grid">
    {#each $columns as column}
        <Column {column} />
    {/each}
</div>

<style>
    .grid {
        display: flex;
        align-self: center;
    }
</style>
