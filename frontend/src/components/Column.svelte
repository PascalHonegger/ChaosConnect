<script lang="ts">
    import Cell from "./Cell.svelte";
    import type { Column } from "../lib/GameState";
    import Piece from "./Piece.svelte";

    export let column: Column;
</script>

<div class="column">
    {#each column.cells as cell}
        <Cell {cell} />
    {/each}
    {#each column.queue as piece}
        <Piece {piece} />
    {/each}
</div>

<style>
    .column {
        display: flex;
        flex-direction: column-reverse;
    }
</style>
