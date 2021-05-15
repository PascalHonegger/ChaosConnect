
import type { ClientReadableStream, Metadata } from 'grpc-web';
import { Empty } from '../gen/common_pb';
import { Faction, GameUpdateEvent, PlacePieceRequest, StartPlayingRequest } from '../gen/game_pb';
import { ChaosConnectServiceClient } from '../gen/JoestarServiceClientPb';

const client = new ChaosConnectServiceClient('/api');

export function getGameUpdates(metadata: Metadata): ClientReadableStream<GameUpdateEvent> {
    return client.getGameUpdates(new Empty(), metadata) as ClientReadableStream<GameUpdateEvent>;
}

export async function startPlaying(faction: Faction, metadata: Metadata): Promise<Empty> {
    const request = new StartPlayingRequest();
    request.setFaction(faction);
    return client.startPlaying(request, metadata);
}

export async function stopPlaying(metadata: Metadata): Promise<Empty> {
    return client.stopPlaying(new Empty(), metadata);
}

export async function placePiece(column: number, metadata: Metadata): Promise<Empty> {
    const request = new PlacePieceRequest();
    request.setColumn(column);
    return client.placePiece(request, metadata);
}
