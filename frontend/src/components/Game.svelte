<script lang="ts">
    import type { ClientReadableStream } from "grpc-web";
    import Column from "./Grid.svelte";
    import { newEmpty } from "../lib/CommonClient";
    import {
        columns,
        factions,
        gameState
    } from "../stores/GameState";
    import type { GameUpdateEvent } from "../gen/game_pb";
    import type { ChaosConnectServiceClient } from "../gen/JoestarServiceClientPb";
    import { authMetadata } from "../stores/Auth";
    import { onMount } from "svelte";
    import Piece from "./Piece.svelte";
import Grid from "./Grid.svelte";

    export let client: ChaosConnectServiceClient;

    onMount(() => {
        const updateStream = client.getGameUpdates(
            newEmpty(),
            $authMetadata
        ) as ClientReadableStream<GameUpdateEvent>;
        updateStream.on("data", (updateEvent) => gameState.apply(updateEvent));
    });
</script>

<div class="game">
    <ul class="factions">
        {#each [...$factions] as [faction, players]}
            <li>
                <Piece {faction} />
                {#if players.length > 0}
                    <ul class="players">
                        {#each players as player}
                            <li>
                                {player.displayName}
                                {#if player.disconnected}(Disconnected){/if}
                            </li>
                        {/each}
                    </ul>
                {:else}
                    <div class="no-players-message">No Players yet</div>
                {/if}
            </li>
        {/each}
    </ul>

    <Grid />
</div>

<style>
    ul {
        list-style: none;
    }

    .factions > li {
        padding-top: 0.5rem;
        border-bottom: 3px solid black;
        padding-bottom: 0.5rem;
    }

    .players,
    .no-players-message {
        padding-left: var(--piece-size);
    }

    .game {
        display: grid;
        grid-template-columns: 1fr 6fr;
    }
</style>
