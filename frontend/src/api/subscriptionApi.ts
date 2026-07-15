import {BASE_URL, fetchText, fetchWithCreds} from "./apiClient";

export const subscriptionApi = {
    getSubscriptions: () => fetchWithCreds(`${BASE_URL}/subscription/subscribtions`),

    subscribe: (city: string, heat: boolean, cold: boolean, wind: boolean, precip: boolean) =>
        fetchText(`${BASE_URL}/subscription/subscribe?city=${city}&notifyHeat=${heat}&notifyCold=${cold}&notifyWind=${wind}&notifyPrecipitation=${precip}`),

    unsubscribe: (id: number) => fetchText(`${BASE_URL}/subscription/unsubscribe/${id}`),
};