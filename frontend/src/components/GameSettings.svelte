<script lang="ts">
    import twemoji from "../lib/Twemoji";
    import type { TokenResponse } from "../gen/authentication_pb";
    import Spinner from "./Spinner.svelte";
    import { authMetadata, token, userType } from "../stores/Auth";
    import {
        register,
        setDisplayName,
        setPassword,
    } from "../lib/WebLoginClient";
    import { player } from "../stores/GameState";

    let updatePasswordRequest: Promise<void> | null = null;
    let updateDisplayNameRequest: Promise<void> | null = null;
    let upgradeToRegularUserRequest: Promise<void> | null = null;
    let isLoading = false;

    let newUsername = "";
    let newPassword = "";
    let newDisplayName = $player?.displayName ?? "";

    async function handleResponse(
        request: Promise<TokenResponse>
    ): Promise<void> {
        isLoading = true;
        try {
            const tokenResponse = await request;
            token.set(tokenResponse.getJwtToken());
        } catch (error) {
            console.warn(error);
            throw error;
        } finally {
            isLoading = false;
        }
    }

    function updatePassword() {
        updatePasswordRequest = handleResponse(
            setPassword(newPassword, $authMetadata)
        ).then(() => {
            newPassword = "";
        });
    }

    function updateDisplayName() {
        updateDisplayNameRequest = handleResponse(
            setDisplayName(newDisplayName, $authMetadata)
        );
    }

    function updateToRegularUser() {
        upgradeToRegularUserRequest = handleResponse(
            register(newUsername, newPassword, newDisplayName, $authMetadata)
        ).then(() => {
            newUsername = "";
            newPassword = "";
        });
    }
</script>

<div class="wrapper">
    <div class="card">
        <h2>Set New Display Name</h2>
        <form class="content" on:submit|preventDefault={updateDisplayName}>
            <label for="display-name">New display name</label>
            <input
                id="display-name"
                type="text"
                autocomplete="nickname"
                disabled={isLoading}
                bind:value={newDisplayName}
            />

            {#if updateDisplayNameRequest != null}
                {#await updateDisplayNameRequest}
                    <div class="card-overlay">
                        <Spinner />
                    </div>
                {:then}
                    <div use:twemoji>
                        ✔ You'll see your new name when you score next time!
                    </div>
                {:catch}
                    <div use:twemoji>❌ Error while updating display name</div>
                {/await}
            {/if}

            <button
                use:twemoji
                type="submit"
                class="save-button"
                disabled={isLoading}>💾</button
            >
        </form>
    </div>
    {#if $userType === "REGULAR"}
        <div class="card">
            <h2>Set New Password</h2>
            <form class="content" on:submit|preventDefault={updatePassword}>
                <label for="password">New password</label>
                <input
                    id="password"
                    type="password"
                    autocomplete="new-password"
                    disabled={isLoading}
                    bind:value={newPassword}
                />

                {#if updatePasswordRequest != null}
                    {#await updatePasswordRequest}
                        <div class="card-overlay">
                            <Spinner />
                        </div>
                    {:then}
                        <div use:twemoji>✔ New password was set</div>
                    {:catch}
                        <div use:twemoji>❌ Error while changing password</div>
                    {/await}
                {/if}

                <button
                    use:twemoji
                    type="submit"
                    class="save-button"
                    disabled={isLoading}>💾</button
                >
            </form>
        </div>
    {:else}
        <div class="card">
            <h2>Upgrade to Regular Account</h2>
            <form
                class="content"
                on:submit|preventDefault={updateToRegularUser}
            >
                <label for="username">Username</label>
                <input
                    id="username"
                    type="text"
                    autocomplete="username"
                    disabled={isLoading}
                    bind:value={newUsername}
                />
                <label for="password">New password</label>
                <input
                    id="password"
                    type="password"
                    autocomplete="new-password"
                    disabled={isLoading}
                    bind:value={newPassword}
                />

                {#if upgradeToRegularUserRequest != null}
                    {#await upgradeToRegularUserRequest}
                        <div class="card-overlay">
                            <Spinner />
                        </div>
                    {:then}
                        <div use:twemoji>
                            ✔ Your account was upgraded to a regular user
                        </div>
                    {:catch}
                        <div use:twemoji>❌ Error while upgrading account</div>
                    {/await}
                {/if}

                <button
                    use:twemoji
                    type="submit"
                    class="save-button"
                    disabled={isLoading}>💾</button
                >
            </form>
        </div>
    {/if}
</div>

<style>
    .wrapper {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: space-evenly;
    }
    .wrapper > .card {
        flex: 1 0 auto;
        min-width: 400px;
        max-width: 600px;
        margin: calc(var(--spacing) / 2);
    }
    .content {
        display: grid;
        grid-template-columns: 150px auto auto;
    }
    .save-button {
        font-size: 20pt;
        grid-column: 3 / 4;
        margin: 0;
    }
    h2 {
        margin-top: 0;
    }
    input {
        grid-column: 2 / 4;
    }
    .content > div {
        grid-column: 1 / 3;
    }
</style>
