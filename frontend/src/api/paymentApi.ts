import {BASE_URL, fetchWithCreds} from "./apiClient";

export const paymentApi = {
    getHistory: (page: number, size: number) => fetchWithCreds(`${BASE_URL}/payment/history?page=${page}&size=${size}`),

    createPayment: (level: string) => fetchWithCreds(`${BASE_URL}/payment/create?level=${level}`, {method: 'POST'}),

    confirmPayment: (paymentId: string) => fetchWithCreds(`${BASE_URL}/payment/confirm/${paymentId}`, {method: 'POST'}),
};