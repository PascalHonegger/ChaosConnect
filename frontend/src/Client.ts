import { EchoServiceClient } from "./gen/EchoServiceClientPb";
import { EchoRequest, ServerStreamingEchoRequest } from "./gen/echo_pb";

export default new EchoServiceClient("/api");

export function newTodoRequest(message: string): EchoRequest {
    const req = new EchoRequest();
    req.setMessage(message);
    return req;
}

export function newStreamingRequest(message: string, count: number = 10, interval: number = 500): ServerStreamingEchoRequest {
    const req = new ServerStreamingEchoRequest();
    req.setMessage(message);
    req.setMessageCount(count);
    req.setMessageInterval(interval);
    return req;
}