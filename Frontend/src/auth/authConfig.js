import { environment } from "../utils/enviroment.ts";

export const msalConfig = {
    auth: {
        clientId: environment.azure.clientId,
        authority: environment.azure.authority,
        redirectUri: environment.azure.redirectUri,
        postLogoutRedirectUri: "http://localhost:5173",
    },
    cache: {
        cacheLocation: "localStorage",
        storeAuthStateInCookie: false,
    },
};

export const apiScope = `api://${environment.azure.clientId}/access_as_user`;

export const loginRequest = {
    scopes: ["openid", "profile", apiScope],
    prompt: "select_account",
};
