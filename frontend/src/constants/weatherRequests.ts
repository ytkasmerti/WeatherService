export type RequestType = 'current' | 'coords' | 'forecast' | 'history' | 'hourly' | 'filter';

export const menuMapping: Record<RequestType, { title: string; desc: string; level: string }> = {
    current: {title: 'Текущая погода', desc: 'Получение текущих погодных данных по названию города.', level: 'FREE+'},
    coords: {title: 'Погода по координатам', desc: 'Текущая погода в любой точке мира по широте и долготе.', level: 'FREE+'},
    forecast: {title: 'Прогноз', desc: 'Прогноз погоды до 15 дней.', level: 'BASIC+'},
    history: {title: 'История', desc: 'Данные за период или конкретную дату (до 5 лет назад).', level: 'BASIC+'},
    hourly: {title: 'Почасовой прогноз', desc: 'Детальный прогноз погоды конкретной даты по часам.', level: 'PREMIUM'},
    filter: {title: 'Фильтрация', desc: 'Поиск дней в прогнозе, соответствующих погодным условиям.', level: 'PREMIUM'}
};