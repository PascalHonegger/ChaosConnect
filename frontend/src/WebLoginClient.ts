import {WebLoginServiceClient} from './gen/JoestarServiceClientPb';
import {LoginRequest, PlayWithoutAccountRequest, RegisterRequest} from "./gen/authentication_pb";

export default new WebLoginServiceClient("/api");

export function newPlayRequest(displayName: string): PlayWithoutAccountRequest {
    const loginRequest = new PlayWithoutAccountRequest();
    loginRequest.setDisplayName(displayName);
    return loginRequest;
}

export function newLoginRequest(username: string, password: string): LoginRequest {
    const loginRequest = new LoginRequest();
    loginRequest.setUsername(username);
    loginRequest.setPassword(password);
    return loginRequest;
}

export function newRegisterRequest(username: string, password: string, displayName: string): RegisterRequest {
    const registerRequest = new RegisterRequest();
    registerRequest.setUsername(username);
    registerRequest.setPassword(password);
    registerRequest.setDisplayName(displayName);
    return registerRequest;
}
