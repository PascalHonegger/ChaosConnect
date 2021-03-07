<script lang="ts">
    import type { EchoServiceClient } from "./gen/EchoServiceClientPb";
    import { newStreamingRequest, newTodoRequest } from "./Client";
    export let client: EchoServiceClient;
    let responses: any[] = [];
    const listResponse = client.echo(newTodoRequest("OneTimeTest"), null);
    const streamingResponse = client.serverStreamingEcho(newStreamingRequest("StreamingTest"));
    streamingResponse.on('data', (d) => responses = [...responses, d]);
    streamingResponse.on('error', (d) => responses = [...responses, d.message]);
    streamingResponse.on('end', () => responses = [...responses, 'deez nutz']);
</script>

<h2>One Time:</h2>
{#await listResponse}
    <p>waiting</p>
{:then result}
    Hey there
    <div>{result.getMessage()}</div>
{:catch error}
    error:
    <p>{error.message}</p>
{/await}
<hr>
<h2>Streaming:</h2>
{#each responses as r}
<div>{r}</div>
{/each}