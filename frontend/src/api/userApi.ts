import {BASE_URL, fetchWithCreds} from "./apiClient";

export const userApi = {
    getProfile: () => fetchWithCreds(`${BASE_URL}/user/profile`),

    setAutoRenewal: (enabled: boolean) => fetchWithCreds(`${BASE_URL}/user/auto-renewal?enabled=${enabled}`),

    changePassword: (oldPassword: string, newPassword: string) =>
        fetchWithCreds(`${BASE_URL}/user/change-password?oldPassword=${oldPassword}&newPassword=${newPassword}`, {method: 'POST'}),

    deleteAccount: (password: string) => fetchWithCreds(`${BASE_URL}/user/delete?password=${password}`, {method: 'DELETE'}),
};