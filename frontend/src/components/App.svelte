<script lang="ts">
    import twemoji from "../lib/Twemoji";
    import TokenRefresher from "./TokenRefresher.svelte";
    import LoginRegister from "./LoginRegister.svelte";
    import GameSettings from "./GameSettings.svelte";
    import { authMetadata, isLoggedIn, token } from "../stores/Auth";
    import Game from "./Game.svelte";
    import { stopPlaying } from "../lib/ChaosConnectClient";
    import { player, playerConnected } from "../stores/GameState";

    let mode: "game" | "settings" = "game";

    function logout(): void {
        if ($player) {
            stopPlaying($authMetadata);
        }
        token.unset();
    }
</script>

<main>
    <h1 use:twemoji>⚔️ ChaosConnect ⚔️</h1>
    {#if $isLoggedIn}
        <TokenRefresher />
        {#if $playerConnected}
            <div>
                Hey <span class="player-name">{$player?.displayName}</span>!
                {#if mode === "game"}
                    <button on:click={logout}>Logout</button>
                    <button on:click={() => stopPlaying($authMetadata)}>Switch Faction</button>
                    <button on:click={() => (mode = "settings")} use:twemoji
                        >⚙</button
                    >
                {:else if mode === "settings"}
                    <button on:click={() => (mode = "game")} use:twemoji>
                        Go back
                    </button>
                {/if}
            </div>
        {/if}
        <!-- Only hide game to prevent unsubscribe and resubscribe to events -->
        <div class:hidden={mode !== "game"}>
            <Game />
        </div>
        {#if mode === "settings"}
            <GameSettings />
        {/if}
    {:else}
        <LoginRegister />
    {/if}
</main>
<footer>
    <a href="https://github.com/twitter/twemoji">Twitter Emoji (Twemoji)</a>
    images licensed under
    <a href="https://creativecommons.org/licenses/by/4.0/">CC-BY 4.0</a>
</footer>

<style>
    .player-name {
        font-weight: bold;
    }

    main {
        padding: 1em;
        margin: 0 auto;
        display: flex;
        flex-direction: column;
    }

    footer {
        text-align: center;
        font-size: small;
        padding: 1em;
        margin: 0 auto;
    }

    h1 {
        color: var(--primary-color);
        font-size: 2em;
        font-weight: 100;
        text-align: center;
    }

    @media only screen and (min-width: 768px) {
        h1 {
            font-size: 3em;
        }
    }

    @media only screen and (min-width: 992px) {
        h1 {
            font-size: 4em;
        }
    }

    @media only screen and (min-width: 1920px) {
        h1 {
            font-size: 5em;
        }
    }
</style>
