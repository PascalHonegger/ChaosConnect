
import { Coordinate } from '../gen/game_pb';
import { ChaosConnectServiceClient } from '../gen/JoestarServiceClientPb';

export default new ChaosConnectServiceClient("/api");

export function newCoordinate(row: number, column: number): Coordinate {
    const coordinate = new Coordinate();
    coordinate.setColumn(column);
    coordinate.setRow(row);
    return coordinate;
}
