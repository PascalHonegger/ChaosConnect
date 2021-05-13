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
        --column-size: calc(var(--piece-size) + 2px);
    }

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
        height: var(--column-size);
        width: var(--column-size);
    }

    .queue,
    .column {
        width: var(--size);
        display: flex;
        flex-direction: column-reverse;
    }

    .disabled {
        background: gray;
    }
</style>
