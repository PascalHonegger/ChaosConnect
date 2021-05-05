
import type { Metadata } from 'grpc-web';
import { Faction, PlacePieceRequest, StartPlayingRequest } from '../gen/game_pb';
import { ChaosConnectServiceClient } from '../gen/JoestarServiceClientPb';

const client = new ChaosConnectServiceClient('/api');

export default client;

export async function startPlaying(faction: Faction, metadata: Metadata) {
    const request = new StartPlayingRequest();
    request.setFaction(faction);
    return client.startPlaying(request, metadata);
}

export async function placePiece(column: number, metadata: Metadata) {
    const request = new PlacePieceRequest();
    request.setColumn(column);
    return client.placePiece(request, metadata);
}
