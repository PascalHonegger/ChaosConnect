<script lang="ts">
    import type { ChaosConnectServiceClient } from "./gen/JoestarServiceClientPb";
    import type { GameUpdateEvent } from "./gen/game_pb";
    import type { ClientReadableStream } from "grpc-web";
    import { newEmpty } from "./CommonClient";
    import { authMetadata } from "./Stores";

    export let client: ChaosConnectServiceClient;
    let gameUpdates: any[] = [];
    // cast is workaround for https://github.com/grpc/grpc-web/issues/950
    const updateStream = client.getGameUpdates(
        newEmpty(),
        $authMetadata
    ) as ClientReadableStream<GameUpdateEvent>;
    updateStream.on("data", (d) => (gameUpdates = [...gameUpdates, d]));
    updateStream.on(
        "error",
        (d) => (gameUpdates = [...gameUpdates, d.message])
    );
    updateStream.on(
        "end",
        () => (gameUpdates = [...gameUpdates, "session ended"])
    );
</script>

{#each gameUpdates as update}
    <div>{update}</div>
{/each}
