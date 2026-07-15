import {BASE_URL, fetchWithCreds} from "./apiClient";

export const weatherApi = {
    getCurrent: (city: string) => fetchWithCreds(`${BASE_URL}/weather/current?city=${city}`),

    getByCoords: (lat: number, lon: number) => fetchWithCreds(`${BASE_URL}/weather/current?lat=${lat}&lon=${lon}`),

    getForecast: (city: string, days: number) => fetchWithCreds(`${BASE_URL}/weather/forecast?city=${city}&days=${days}`),

    getHistory: (city: string, start: string, end: string) =>
        fetchWithCreds(`${BASE_URL}/weather/history?city=${city}&start=${start}&end=${end}`),

    getByDate: (city: string, date: string) => fetchWithCreds(`${BASE_URL}/weather/history/date?city=${city}&date=${date}`),

    getHourly: (city: string, date: string) => fetchWithCreds(`${BASE_URL}/weather/forecast/hourly?city=${city}&date=${date}`),

    getFiltered: (city: string, days: number, filter: string) =>
        fetchWithCreds(`${BASE_URL}/weather/forecast/filter?city=${city}&days=${days}&filterCondition=${filter}`)
};