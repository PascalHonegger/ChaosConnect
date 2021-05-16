<script lang="ts">
    import Cell from "./Cell.svelte";
    import type { Column } from "../lib/GameState";
    import Piece from "./Piece.svelte";
    import { columns, player } from "../stores/GameState";
    import { placePiece } from "../lib/ChaosConnectClient";
    import { authMetadata } from "../stores/Auth";

    let previewColumn: Column | null = null;
</script>

<div class="grid">
    <div>
        {#each $columns as column}
            <div class="preview">
                {#if previewColumn === column && !column.disabled && $player != null}
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
        {#each $columns as column, index}
            <div
                class="column"
                class:disabled={column.disabled}
                on:mouseenter={() => previewColumn = column}
                on:mouseleave={() => previewColumn = null}
                on:click={column.disabled ? undefined : () => placePiece(index, $authMetadata)}
            >
                {#each column.cells as cell}
                    <Cell {cell} />
                {/each}
            </div>
        {/each}
    </div>
</div>

<style>
    :root {
        --column-width: calc(var(--piece-size) + 2px);
        --edge-column-width: calc(var(--column-width) + 1px);
    }

    .grid {
        display: flex;
        flex-direction: column;
        justify-self: center;
        align-self: end;
    }

    .grid > div {
        display: flex;
        flex-direction: row;
    }

    .queue, .preview {
        width: var(--column-width);
    }

    .preview {
        height: var(--column-width);
    }

    .preview:first-child, .queue:first-child, .preview:last-child, .queue:last-child {
        width: var(--edge-column-width);
    }

    .queue,
    .column,
    .preview {
        display: flex;
        flex-direction: column-reverse;
        align-items: center;
    }

    .column {
        border-style: solid;
        border-color: black;
        border-width: 0 1px;
    }

    .column:first-child {
        border-left-width: 2px;
    }

    .column:last-child {
        border-right-width: 2px;
    }

    .disabled {
        background: gray;
    }
</style>
