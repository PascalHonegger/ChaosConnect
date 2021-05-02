<script lang="ts">
    import Cell from "./Cell.svelte";
    import type { Column } from "../lib/GameState";
    import Piece from "./Piece.svelte";
    import { columns, player } from "../stores/GameState";

    let previewColumn: Column | null = null;
</script>

<div class="grid">
    <div>
        {#each $columns as column}
            <div class="preview">
                {#if previewColumn === column}
                    <Piece faction={$player.faction} />
                {/if}
            </div>
        {/each}
    </div>

    <div>
        {#each $columns as column}
            <div class="queue">
                {#each column.queue as piece}
                    <Piece {piece} />
                {/each}
            </div>
        {/each}
    </div>

    <div>
        {#each $columns as column}
            <div
                class="column"
                on:mouseenter={() => (previewColumn = column)}
                on:mouseleave={() => (previewColumn = null)}
            >
                {#each column.cells as cell}
                    <Cell {cell} />
                {/each}
            </div>
        {/each}
    </div>
</div>

<style>
    .grid {
        display: flex;
        flex-direction: column;
        justify-self: center;
    }

    .grid > div {
        display: flex;
        flex-direction: row;
    }

    .preview {
        height: var(--piece-size);
        width: var(--piece-size);
    }

    .queue,
    .column {
        width: var(--piece-size);
        display: flex;
        flex-direction: column-reverse;
    }
</style>
