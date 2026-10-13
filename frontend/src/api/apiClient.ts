const BASE_URL = import.meta.env.VITE_API_URL ?? '/api';

export const fetchWithCreds = async (url: string, options: RequestInit = {}) => {
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

export const fetchText = async (url: string, options: RequestInit = {}) => {
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

export {BASE_URL};