import { derived, Readable, writable } from 'svelte/store';
import jwt_decode, { JwtPayload } from 'jwt-decode';
import type { Metadata } from 'grpc-web';

type PlayerType = 'TEMPORARY' | 'REGULAR';

interface ChaosConnectJwtPayload extends JwtPayload {
    iss: string;
    sub: string;
    aud: string;
    exp: number;
    iat: number;
    player_type: PlayerType;
}

function createToken() {
    const { subscribe, set } = writable<string | null>(localStorage.getItem("token"));

    return {
        subscribe,
        set: (t: string) => {
            set(t);
            localStorage.setItem("token", t);
        },
        unset: () => {
            set(null);
            localStorage.removeItem("token");
        }
    };
}

function tryDecode($token: string) {
    try {
        return jwt_decode<ChaosConnectJwtPayload>($token);
    } catch (ex) {
        console.warn("Error while parsing toking, ignoring", ex);
        return null;
    }
}

export const token = createToken();

const decodedToken: Readable<ChaosConnectJwtPayload | null> = derived(token, $token =>
    $token == null ? null : tryDecode($token)
);

export const isLoggedIn: Readable<boolean> = derived(decodedToken, $decodedToken =>
    // Check Expired is greater than now
    $decodedToken?.exp != null && $decodedToken.exp > new Date().getTime() / 1000
);

export const userIdentifier: Readable<string | null> = derived(decodedToken, $decodedToken =>
    $decodedToken?.sub ?? null
);

export const userType: Readable<PlayerType | null> = derived(decodedToken, $decodedToken =>
    $decodedToken?.player_type ?? null
);

export const authMetadata: Readable<Metadata> = derived(token, $token => {
    if ($token == null) {
        return { Authorization: '' };
    }
    return { Authorization: `Bearer ${$token}` };
});
