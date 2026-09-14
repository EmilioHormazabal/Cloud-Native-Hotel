import { loginRequest } from "./authConfig.js";

function accountFrom(instance) {
    const accounts = instance.getAllAccounts();
    return accounts.length > 0 ? accounts[0] : null;
}

export function getRoles(account) {
    if (!account || !account.idTokenClaims) return [];
    const roles = account.idTokenClaims.roles;
    return Array.isArray(roles) ? roles : [];
}

export function getScopes(account) {
    if (!account || !account.idTokenClaims) return [];
    const scp = account.idTokenClaims.scp;
    if (Array.isArray(scp)) return scp;
    if (typeof scp === "string") return scp.split(" ");
    return [];
}

function decodePayload(token) {
    const b = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(b + "=".repeat((4 - (b.length % 4)) % 4)));
}

export async function getAccessTokenScopes(instance) {
    const accounts = instance.getAllAccounts();
    if (accounts.length === 0) return [];
    const res = await instance.acquireTokenSilent({ ...loginRequest, account: accounts[0] });
    const scp = decodePayload(res.accessToken).scp;
    if (Array.isArray(scp)) return scp;
    if (typeof scp === "string") return scp.split(" ");
    return [];
}

export async function getAccessToken(instance) {
    const account = accountFrom(instance);
    if (!account) throw new Error("Sin sesión: primero inicia sesión");
    try {
        const res = await instance.acquireTokenSilent({ ...loginRequest, account });
        return res.accessToken;
    } catch {
        const res = await instance.acquireTokenRedirect({ ...loginRequest, account });
        return res?.accessToken ?? null;
    }
}
