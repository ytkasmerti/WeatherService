import {useState} from 'react';
import {Button} from '../components/Button/Button';
import {Input} from '../components/Input/Input';
import {Notification} from '../components/Notification/Notification';
import {CalendarPicker} from '../components/CalendarPicker/CalendarPicker';
import {weatherApi} from '../api/authService';
import {FilterPicker} from '../components/FilterPicker/FilterPicker';

type RequestType = 'current' | 'coords' | 'forecast' | 'history' | 'historyDate' | 'hourly' | 'filter';

const menuMapping: Record<RequestType, string> = {
    current: 'Текущая погода',
    coords: 'Погода по координатам',
    forecast: 'Прогноз',
    history: 'История (период)',
    historyDate: 'История (дата)',
    hourly: 'Почасовой прогноз',
    filter: 'Фильтрация'
};

export const WeatherPage = () => {
    const [activeRequest, setActiveRequest] = useState<RequestType | null>(null);
    const [params, setParams] = useState({
        city: '', lat: '', lon: '', date: '', start: '', end: '', days: '7', filter: ''
    });
    const [isLoading, setIsLoading] = useState(false);
    const [result, setResult] = useState<any>(null);
    const [notify, setNotify] = useState<string | null>(null);

    const handleRequest = async () => {
        setIsLoading(true);
        try {
            let data;
            switch (activeRequest) {
                case 'current':
                    data = await weatherApi.getCurrent(params.city);
                    break;
                case 'coords':
                    data = await weatherApi.getByCoords(Number(params.lat), Number(params.lon));
                    break;
                case 'forecast':
                    data = await weatherApi.getForecast(params.city, Number(params.days));
                    break;
                case 'history':
                    data = await weatherApi.getHistory(params.city, params.start, params.end);
                    break;
                case 'historyDate':
                    data = await weatherApi.getByDate(params.city, params.date);
                    break;
                case 'hourly':
                    data = await weatherApi.getHourly(params.city, params.date);
                    break;
                case 'filter':
                    data = await weatherApi.getFiltered(params.city, Number(params.days), params.filter);
                    break;
            }
            setResult(data);
            setNotify("Данные успешно получены!");
        } catch (e: any) {
            setNotify(e.message || "Ошибка");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="weather-container">
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            {!activeRequest ? (
                <div className="menu-grid">
                    {(Object.keys(menuMapping) as RequestType[]).map(type => (
                        <Button key={type} text={menuMapping[type]} onClick={() => setActiveRequest(type)}/>
                    ))}
                </div>
            ) : (
                <div className="form-area">
                    <div className="back-btn-wrapper">
                        <Button text="← Назад" onClick={() => {
                            setActiveRequest(null);
                            setResult(null);
                        }}/>
                    </div>

                    {activeRequest !== 'coords' && (
                        <Input placeholder="Название города" value={params.city}
                               onChange={(e) => setParams({...params, city: e.target.value})}/>
                    )}

                    {activeRequest === 'coords' && (
                        <>
                            <Input placeholder="Широта (lat)" value={params.lat}
                                   onChange={(e) => setParams({...params, lat: e.target.value})}/>
                            <Input placeholder="Долгота (lon)" value={params.lon}
                                   onChange={(e) => setParams({...params, lon: e.target.value})}/>
                        </>
                    )}

                    {(activeRequest === 'forecast' || activeRequest === 'filter') && (
                        <Input placeholder="Количество дней" value={params.days}
                               onChange={(e) => setParams({...params, days: e.target.value})}/>
                    )}

                    {activeRequest === 'filter' && (
                        <FilterPicker onSelect={(val) => setParams({...params, filter: val})}/>
                    )}

                    {(activeRequest === 'historyDate' || activeRequest === 'hourly') && (
                        <CalendarPicker onSelect={(d) => setParams({...params, date: d})}/>
                    )}

                    {activeRequest === 'history' && (
                        <CalendarPicker onRangeSelect={(start, end) => setParams({...params, start, end})}/>
                    )}

                    <Button text="Отправить запрос" onClick={handleRequest} isLoading={isLoading}/>

                    {result && <div className="result-card">
                        <pre>{JSON.stringify(result, null, 2)}</pre>
                    </div>}
                </div>
            )}
        </div>
    );
};