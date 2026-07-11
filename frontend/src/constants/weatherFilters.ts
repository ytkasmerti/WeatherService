import rainIcon from '../assets/weather/rain.svg';
import sunIcon from '../assets/weather/sun.svg';
import cloudIcon from '../assets/weather/cloud.svg';
import fogIcon from '../assets/weather/fog.svg';
import snowIcon from '../assets/weather/snow.svg';
import thunderstormIcon from '../assets/weather/thunderstorm.svg';
import windIcon from '../assets/weather/wind.svg';

export const WEATHER_FILTERS = ['дождь', 'солнце', 'облачно', 'снег', 'ветер', 'гроза', 'туман'];

export const FILTER_ICONS: Record<string, string> = {
    'дождь': rainIcon,
    'солнце': sunIcon,
    'облачно': cloudIcon,
    'снег' : snowIcon,
    'ветер' : windIcon,
    'гроза' : thunderstormIcon,
    'туман' : fogIcon
};