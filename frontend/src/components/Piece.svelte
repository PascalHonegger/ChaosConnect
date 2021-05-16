<script lang="ts">
    import { Faction } from "../gen/game_pb";
    import type { Piece } from "../lib/GameState";
    import { playerMap } from "../stores/GameState";

    export let piece: Piece|null = null;
    export let faction: Faction|null = null;

    $: faction = piece?.faction ?? faction;
    $: playerName = piece == null ? null : ($playerMap.get(piece.owner)?.displayName ?? 'Disconnected player');
</script>

<div
    class="piece"
    class:yellow={faction === Faction.YELLOW}
    class:red={faction === Faction.RED}
    title={playerName != null ? `Placed by ${playerName}` : undefined}
><slot /></div>

<style>
    .piece {
        width: var(--piece-size);
        height: var(--piece-size);
        border-radius: calc(var(--piece-size) / 2);
    }

    .red {
        background: red;
    }

    .yellow {
        background: yellow;
    }
</style>
