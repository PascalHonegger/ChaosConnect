<script lang="ts">
    import type {Cell} from "../lib/GameState";
    import Piece from "./Piece.svelte";

    export let cell: Cell;
</script>

<div class="cell" class:scored={cell.scored}>
    {#if cell.piece}
        <Piece piece={cell.piece}/>
    {/if}
</div>

<style>
    .cell {
        border: 1px solid black;
        width: var(--piece-size);
        height: var(--piece-size);
    }

    .cell:first-child {
        border-bottom: 2px solid black;
    }

    .cell:last-child {
        border-top: 2px solid black;
    }

    .scored {
        background: gold;
    }
</style>
