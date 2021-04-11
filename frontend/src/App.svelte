<script lang="ts">
    import twemoji from "./Twemoji";

    import JoestarTest from "./JoestarTest.svelte";
    import TokenRefresher from "./TokenRefresher.svelte";
    import chaosConnectClient from "./ChaosConnectClient";
    import webLoginClient from "./WebLoginClient";

    import LoginRegister from "./LoginRegister.svelte";
    import { isLoggedIn, token } from "./Stores";
	import Grid from "./Grid.svelte";
</script>

<main>
    <h1 use:twemoji>⚔️ ChaosConnect ⚔️</h1>
    {#if $isLoggedIn}
        <button on:click={() => token.unset()}>Logout</button>
        <TokenRefresher client={webLoginClient}/>
		<Grid client={chaosConnectClient} />
    {:else}
        <LoginRegister client={webLoginClient} />
    {/if}
</main>
<footer>
    <a href="https://github.com/twitter/twemoji">Twitter Emoji (Twemoji)</a>
    images licensed under
    <a href="https://creativecommons.org/licenses/by/4.0/">CC-BY 4.0</a>
</footer>

<style>
    main {
        text-align: center;
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
    }
</style>
