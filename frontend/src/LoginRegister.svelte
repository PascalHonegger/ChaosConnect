<script lang="ts">
    import type { WebLoginServiceClient } from "./gen/JoestarServiceClientPb";
    import { token } from "./Stores";
    import { newLoginRequest } from "./WebLoginClient";

    export let client: WebLoginServiceClient;
    const loginRequest = client
        .login(newLoginRequest("Pascal", "123"), null)
        .then((response) => {
            token.set(response.getJwtToken());
            return true;
        })
        .catch((error) => {
            console.warn(error);
            token.unset();
            return false;
        })
        .then(loggedIn => {
            // TODO clear inputs once those exist
            return loggedIn;
        });
</script>

{#await loginRequest}
    <p>Logging in...</p>
{:then result}
{#if result}
    Logged in, redirecting...
{:else}
    Login failed, please try again
{/if}
{/await}
