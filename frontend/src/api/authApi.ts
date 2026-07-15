import {BASE_URL, fetchWithCreds} from "./apiClient";

export const authApi = {
    login: (email: string, password: string) => {
        const params = new URLSearchParams({email, password});
        return fetchWithCreds(`${BASE_URL}/auth/login?${params}`, {method: 'POST'});
    },

    register: (email: string, password: string, confirmPassword: string) => {
        const params = new URLSearchParams({email, password, confirmPassword});
        return fetchWithCreds(`${BASE_URL}/auth/register?${params}`, {method: 'POST'});
    },

    forgotPassword: (email: string) => {
        return fetchWithCreds(`${BASE_URL}/auth/forgot-password?email=${encodeURIComponent(email)}`, {method: 'POST'});
    },

    resetPassword: (email: string, code: string, newPassword: string) => {
        const params = new URLSearchParams({email, code, newPassword});
        return fetchWithCreds(`${BASE_URL}/auth/reset-password?${params}`, {method: 'POST'});
    },

    check: () => {
        return fetchWithCreds(`${BASE_URL}/auth/check`);
    },

    logout: () => {
        return fetch(`${BASE_URL}/auth/logout`, {credentials: 'include'});
    }
};