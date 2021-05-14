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

    .scored {
        background: gold;
    }
</style>
