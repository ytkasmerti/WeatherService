const BASE_URL = 'http://localhost:8080';

const fetchWithCreds = async (url: string, options: RequestInit = {}) => {
    const response = await fetch(url, {
        ...options,
        credentials: 'include',
    });

    const data = await response.json();

    if (!response.ok || data.error) {
        throw new Error(data.message || "Ошибка операции");
    }
    return data;
};

const fetchText = async (url: string, options: RequestInit = {}) => {
    const response = await fetch(url, {
        ...options,
        credentials: 'include',
    });

    const text = await response.text();
    if (!response.ok) {
        throw new Error(text || "Ошибка сервера");
    }
    return text;
};

export const authApi = {
    login: async (email: string, password: string) => {
        const params = new URLSearchParams({email, password});
        return fetchWithCreds(`${BASE_URL}/auth/login?${params}`, {method: 'POST'});
    },
    register: async (email: string, password: string, confirmPassword: string) => {
        const params = new URLSearchParams({email, password, confirmPassword});
        return fetchWithCreds(`${BASE_URL}/auth/register?${params}`, {method: 'POST'});
    },
    forgotPassword: async (email: string) => {
        return fetchWithCreds(`${BASE_URL}/auth/forgot-password?email=${encodeURIComponent(email)}`, {method: 'POST'});
    },
    resetPassword: async (email: string, code: string, newPassword: string) => {
        const params = new URLSearchParams({email, code, newPassword});
        return fetchWithCreds(`${BASE_URL}/auth/reset-password?${params}`, {method: 'POST'});
    }
};

export const weatherApi = {
    getCurrent: (city: string) => fetchWithCreds(`${BASE_URL}/weather/current?city=${city}`),
    getByCoords: (lat: number, lon: number) => fetchWithCreds(`${BASE_URL}/weather/current?lat=${lat}&lon=${lon}`),
    getForecast: (city: string, days: number) => fetchWithCreds(`${BASE_URL}/weather/forecast?city=${city}&days=${days}`),
    getHistory: (city: string, start: string, end: string) => fetchWithCreds(`${BASE_URL}/weather/history?city=${city}&start=${start}&end=${end}`),
    getByDate: (city: string, date: string) => fetchWithCreds(`${BASE_URL}/weather/history/date?city=${city}&date=${date}`),
    getHourly: (city: string, date: string) => fetchWithCreds(`${BASE_URL}/weather/forecast/hourly?city=${city}&date=${date}`),
    getFiltered: (city: string, days: number, filter: string) => fetchWithCreds(`${BASE_URL}/weather/forecast/filter?city=${city}&days=${days}&filterCondition=${filter}`),
};

export const userApi = {
    getProfile: () => fetchWithCreds(`${BASE_URL}/user/profile`),
    setAutoRenewal: (enabled: boolean) => fetchWithCreds(`${BASE_URL}/user/auto-renewal?enabled=${enabled}`),
    changePassword: (oldPassword: string, newPassword: string) =>
        fetchWithCreds(`${BASE_URL}/user/change-password?oldPassword=${oldPassword}&newPassword=${newPassword}`, {method: 'POST'}),
    deleteAccount: (password: string) =>
        fetchWithCreds(`${BASE_URL}/user/delete?password=${password}`, {method: 'DELETE'}),
};

export const paymentApi = {
    getHistory: (page: number, size: number) =>
        fetchWithCreds(`${BASE_URL}/payment/history?page=${page}&size=${size}`),
    createPayment: (level: string) =>
        fetchWithCreds(`${BASE_URL}/payment/create?level=${level}`, {method: 'POST'}),
    confirmPayment: (paymentId: string) =>
        fetchWithCreds(`${BASE_URL}/payment/confirm/${paymentId}`, {method: 'POST'}),
};

export const subscriptionApi = {
    getSubscriptions: () => fetchWithCreds(`${BASE_URL}/subscription/subscribtions`),
    subscribe: (city: string, heat: boolean, cold: boolean, wind: boolean, precip: boolean) =>
        fetchText(`${BASE_URL}/subscription/subscribe?city=${city}&notifyHeat=${heat}&notifyCold=${cold}&notifyWind=${wind}&notifyPrecipitation=${precip}`),
    unsubscribe: (id: number) =>
        fetchText(`${BASE_URL}/subscription/unsubscribe/${id}`),
};