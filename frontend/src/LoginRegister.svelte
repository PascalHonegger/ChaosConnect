<script lang="ts">
    import twemoji from "./Twemoji";
    import type { TokenResponse } from "./gen/authentication_pb";

    import type { WebLoginServiceClient } from "./gen/JoestarServiceClientPb";
    import Spinner from "./Spinner.svelte";
    import { token } from "./Stores";
    import {
        newLoginRequest,
        newPlayRequest,
        newRegisterRequest,
    } from "./WebLoginClient";

    export let client: WebLoginServiceClient;

    let request: Promise<boolean> | null = null;
    let isLoading = false;

    let username = "";
    let password = "";
    let displayName = "";

    type FormMode = "play" | "login" | "register";

    let mode: FormMode = "play";

    const buttonMapping: Record<FormMode, string> = {
        play: "Play as Guest",
        login: "Login",
        register: "Register",
    };
    const allModes = Object.keys(buttonMapping) as FormMode[];

    $: hasUsername = mode === "login" || mode === "register";
    $: hasPassword = mode === "login" || mode === "register";
    $: hasDisplayName = mode === "play" || mode === "register";

    async function handleResponse(
        request: Promise<TokenResponse>
    ): Promise<boolean> {
        isLoading = true;
        try {
            const tokenResponse = await request;
            token.set(tokenResponse.getJwtToken());
            return true;
        } catch (error) {
            console.warn(error);
            token.unset();
            return false;
        } finally {
            isLoading = false;
        }
    }

    function submitForm() {
        switch (mode) {
            case "play":
                request = handleResponse(
                    client.playWithoutAccount(newPlayRequest(displayName), null)
                );
                break;
            case "login":
                request = handleResponse(
                    client.login(newLoginRequest(username, password), null)
                );
                break;
            case "register":
                request = handleResponse(
                    client.register(
                        newRegisterRequest(username, password, displayName),
                        null
                    )
                );
                break;
        }
    }
</script>

<div class="wrapper card">
    {#each allModes as m}
        <button
            type="button"
            class="mode-button"
            class:active-mode={mode === m}
            on:click={() => (mode = m)}
            disabled={isLoading}
        >
            {buttonMapping[m]}
        </button>
    {/each}
    <form class="content" on:submit|preventDefault={submitForm}>
        {#if hasUsername}
            <label for="username">Username</label>
            <input
                id="username"
                type="text"
                autocomplete="username"
                disabled={isLoading}
                bind:value={username}
            />
        {/if}
        {#if hasPassword}
            <label for="password">Password</label>
            <input
                id="password"
                type="password"
                autocomplete={mode === "login"
                    ? "current-password"
                    : "new-password"}
                disabled={isLoading}
                bind:value={password}
            />
        {/if}
        {#if hasDisplayName}
            <label for="display-name">Nickname</label>
            <input
                id="display-name"
                type="text"
                autocomplete="nickname"
                disabled={isLoading}
                bind:value={displayName}
            />
        {/if}

        <button
            use:twemoji
            type="submit"
            class="form-row submit-button"
            disabled={isLoading}
        >
            Start 🚀
        </button>
    </form>

    {#if request != null}
        {#await request}
            <div class="card-overlay">
                <Spinner />
            </div>
        {:then loggedIn}
            <div class="form-row">
                {#if loggedIn}
                    Logged in, redirecting...
                {:else}
                    Login failed, please try again
                {/if}
            </div>
        {/await}
    {/if}
</div>

<style>
    .wrapper {
        max-width: 600px;
        margin-left: auto;
        margin-right: auto;
        padding: 10px;
    }
    .content {
        display: grid;
        grid-template-columns: 25% 75%;
    }
    .form-row {
        grid-column: 1 / 3;
        margin-left: auto;
        margin-right: auto;
    }
    .submit-button {
        width: 200px;
        font-size: 25pt;
    }
    .mode-button {
        width: 120px;
    }
    .active-mode {
        background-color: lightblue;
    }
</style>
