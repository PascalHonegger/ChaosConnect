import {WebLoginServiceClient} from './gen/JoestarServiceClientPb';
import {LoginRequest} from "./gen/authentication_pb";

export default new WebLoginServiceClient("/api");

export function newLoginRequest(username: string, password: string): LoginRequest {
    const loginRequest = new LoginRequest();
    loginRequest.setUsername(username);
    loginRequest.setPassword(password);
    return loginRequest;
}
