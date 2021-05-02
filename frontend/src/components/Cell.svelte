<script lang="ts">
    import type { Cell } from "../lib/GameState";
    import Piece from "./Piece.svelte";

    export let cell: Cell;
</script>

<div class="cell" class:disabled={cell.disabled}>
    {#if cell.piece}
        <Piece piece={cell.piece} /> 
    {/if}
</div>

<style>
    .cell {
        border: 1px solid black;
        width: var(--piece-size);
        height: var(--piece-size);
    }

    .disabled {
        background: gray;
    }
</style>
