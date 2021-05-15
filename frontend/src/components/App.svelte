<script lang="ts">
    import twemoji from "../lib/Twemoji";
    import TokenRefresher from "./TokenRefresher.svelte";
    import LoginRegister from "./LoginRegister.svelte";
    import { authMetadata, isLoggedIn, token } from "../stores/Auth";
    import Game from "./Game.svelte";
    import { stopPlaying } from "../lib/ChaosConnectClient";
    import { player } from "../stores/GameState";
    import { onMount } from "svelte";   

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
        <button on:click={logout}>Logout</button>
        <TokenRefresher />
        <Game />
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
        font-size: 4em;
        font-weight: 100;
        text-align: center;
    }
</style>
