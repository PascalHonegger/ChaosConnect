import { ChaosConnectServiceClient } from './gen/JoestarServiceClientPb';
import { Coordinate } from './gen/game_pb';
import { Empty } from './gen/common_pb';

export default new ChaosConnectServiceClient("/api");

export function newCoordinate(row: number, column: number): Coordinate {
    const coordinate = new Coordinate();
    coordinate.setColumn(column);
    coordinate.setRow(row);
    return coordinate;
}

export function newEmpty(): Empty {
    return new Empty();
}
