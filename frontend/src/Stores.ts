import { derived, Readable, writable } from 'svelte/store';
import jwt_decode, { JwtPayload } from 'jwt-decode';
import type { Metadata } from 'grpc-web';

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
        return jwt_decode<JwtPayload>($token);
    } catch(ex) {
        console.warn("Error while parsing toking, ignoring", ex);
        return {};
    }
}

export const token = createToken();

export const isLoggedIn: Readable<boolean> = derived(token, $token => {
    if ($token == null) {
        return false;
    }
    const decoded = tryDecode($token);
    // Check Expired is greater than now
    return decoded.exp != null && +decoded.exp > new Date().getTime() / 1000;
});

export const userIdentifier: Readable<string | null> = derived(token, $token => {
    if ($token == null) {
        return null;
    }
    const decoded = tryDecode($token);
    return decoded.sub ?? null;
});

export const authMetadata: Readable<Metadata> = derived(token, $token => {
    if ($token == null) {
        return { Authorization: '' };
    }
    return { Authorization: `Bearer ${$token}` };
});
