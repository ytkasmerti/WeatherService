import {weatherApi} from '../api/authService';
import {type RequestType} from '../constants/weatherRequests';

export const fetchWeatherData = async (type: RequestType | null, params: any) => {
    switch (type) {
        case 'current':
            return await weatherApi.getCurrent(params.city);
        case 'coords':
            return await weatherApi.getByCoords(Number(params.lat), Number(params.lon));
        case 'forecast':
            return await weatherApi.getForecast(params.city, Number(params.days));
        case 'history':
            return params.start
                ? await weatherApi.getHistory(params.city, params.start, params.end)
                : await weatherApi.getByDate(params.city, params.date);
        case 'hourly':
            return await weatherApi.getHourly(params.city, params.date);
        case 'filter':
            return await weatherApi.getFiltered(params.city, Number(params.days), params.filter);
        default:
            return null;
    }
};