<script lang="ts">
    import type { Error } from "grpc-web";
    import { StatusCode } from "grpc-web";
    import { authMetadata, token } from "../stores/Auth";
    import { onMount } from "svelte";
    import { renewToken } from "../lib/WebLoginClient";

    const retryTimeout = 5000;

    onMount(async () => {
        await tryRenewToken();
    });

    async function tryRenewToken() {
        try {
            const tokenResponse = await renewToken($authMetadata);
            token.set(tokenResponse.getJwtToken());
        } catch (error) {
            if ((<Error>error).code === StatusCode.UNAUTHENTICATED) {
                // Our current token is no longer valid => logout
                token.unset();
            } else {
                console.warn(error);
                setTimeout(async () => {
                    await tryRenewToken();
                }, retryTimeout);
            }
        }
    }
</script>
