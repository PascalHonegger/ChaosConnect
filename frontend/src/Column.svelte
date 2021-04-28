<script lang="ts">
    import Cell from "./Cell.svelte";
    import type { Column } from "./GameState";

    export let column: Column;
    
</script>

<div class="column" >
    {#each column.cells as _ }
        <Cell />
    {/each}
</div>

<style>
    .column {
        display: flex;
        flex-direction: column-reverse;
    }
</style>
