<script lang="ts">
    import {playersByFaction} from "../stores/GameState";
    import Piece from "./Piece.svelte";
</script>

<ul class="factions">
    {#each [...$playersByFaction] as [faction, players]}
        <li>
            <Piece {faction} />
            {#if players.length > 0}
                <ul class="players">
                    {#each players as player}
                        <li>
                            {player.displayName} with {player.score} points
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
</style>
