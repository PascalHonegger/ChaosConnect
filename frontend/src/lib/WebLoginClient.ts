import type { Metadata } from "grpc-web";
import { LoginRequest, PlayWithoutAccountRequest, RegisterRequest, TokenResponse, UpdateMetaStateRequest } from "../gen/authentication_pb";
import { Empty } from "../gen/common_pb";
import { WebLoginServiceClient } from "../gen/JoestarServiceClientPb";

const client = new WebLoginServiceClient('/api');

export async function playWithoutAccount(displayName: string, metadata: Metadata): Promise<TokenResponse> {
    const request = new PlayWithoutAccountRequest();
    request.setDisplayName(displayName);
    return client.playWithoutAccount(request, metadata);
}

export async function login(username: string, password: string, metadata: Metadata): Promise<TokenResponse> {
    const request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);
    return client.login(request, metadata);
}

export async function register(username: string, password: string, displayName: string, metadata: Metadata): Promise<TokenResponse> {
    const request = new RegisterRequest();
    request.setUsername(username);
    request.setPassword(password);
    request.setDisplayName(displayName);
    return client.register(request, metadata);
}

export async function renewToken(metadata: Metadata): Promise<TokenResponse> {
    return client.renewToken(new Empty(), metadata);
}

export async function setDisplayName(newDisplayName: string, metadata: Metadata): Promise<TokenResponse> {
    const request = new UpdateMetaStateRequest();
    request.setDisplayName(newDisplayName);
    return client.updateMetaState(request, metadata);
}

export async function setPassword(newPassword: string, metadata: Metadata): Promise<TokenResponse> {
    const request = new UpdateMetaStateRequest();
    request.setPassword(newPassword);
    return client.updateMetaState(request, metadata);
}
