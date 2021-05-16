<script lang="ts">
    import twemoji from "../lib/Twemoji";
    import type { Cell } from "../lib/GameState";
    import Piece from "./Piece.svelte";

    export let cell: Cell;
</script>

<div class="cell">
    {#if cell.piece}
        <Piece piece={cell.piece}>
            {#if cell.scored}
                <div class="score-indicator" use:twemoji>⭐</div>
            {/if}
        </Piece>
    {/if}
</div>

<style>
    .cell {
        border-color: black;
        border-width: 1px 0;
        border-style: solid;
        width: var(--piece-size);
        height: var(--piece-size);
    }

    .cell:first-child {
        border-bottom-width: 2px;
    }

    .cell:last-child {
        border-top-width: 2px;
    }

    .score-indicator {
        position: absolute;
        display: flex;
        place-content: center;
        place-items: center;
        width: var(--piece-size);
        height: var(--piece-size);
    }
</style>
