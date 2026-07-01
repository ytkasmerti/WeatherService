import {useState} from 'react';
import {Button} from '../components/Button/Button';
import {Input} from '../components/Input/Input';
import {Notification} from '../components/Notification/Notification';
import {CalendarPicker} from '../components/CalendarPicker/CalendarPicker';
import {weatherApi} from '../api/authService';
import {FilterPicker} from '../components/FilterPicker/FilterPicker';

type RequestType = 'current' | 'coords' | 'forecast' | 'history' | 'hourly' | 'filter';

const menuMapping: Record<RequestType, { title: string; desc: string; level: string }> = {
    current: {title: 'Текущая погода', desc: 'Получение текущих погодных данных по названию города.', level: 'FREE+'},
    coords: {
        title: 'Погода по координатам',
        desc: 'Текущая погода в любой точке мира по широте и долготе.',
        level: 'FREE+'
    },
    forecast: {title: 'Прогноз', desc: 'Прогноз погоды до 15 дней.', level: 'BASIC+'},
    history: {title: 'История', desc: 'Данные за период или конкретную дату (до 5 лет назад).', level: 'BASIC+'},
    hourly: {title: 'Почасовой прогноз', desc: 'Детальный прогноз погоды по часам.', level: 'PREMIUM'},
    filter: {title: 'Фильтрация', desc: 'Поиск дней в прогнозе, соответствующих погодным условиям.', level: 'PREMIUM'}
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
                    data = params.start ? await weatherApi.getHistory(params.city, params.start, params.end)
                        : await weatherApi.getByDate(params.city, params.date);
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
        <div style={{padding: '20px', maxWidth: '800px', margin: '0 auto'}}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            <h1 style={{textAlign: 'center', marginBottom: '40px'}}>WeatherService</h1>

            {!activeRequest ? (
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                    gap: '20px'
                }}>
                    {(Object.keys(menuMapping) as RequestType[]).map(type => (
                        <div key={type} style={{
                            padding: '20px',
                            border: '1px solid #eee',
                            borderRadius: '12px',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '12px',
                            backgroundColor: '#fff'
                        }}>
                            <Button text={menuMapping[type].title} onClick={() => setActiveRequest(type)}/>
                            <div style={{fontSize: '15px', color: '#333', lineHeight: '1.5'}}>
                                <p style={{margin: '0 0 8px 0'}}>{menuMapping[type].desc}</p>
                                <p style={{margin: 0, fontWeight: 'bold', color: '#007bff'}}>
                                    Уровень: {menuMapping[type].level}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div style={{display: 'flex', flexDirection: 'column', gap: '15px'}}>
                    <div style={{marginBottom: '20px'}}>
                        <Button text="← Назад" onClick={() => {
                            setActiveRequest(null);
                            setResult(null);
                        }}/>
                    </div>

                    {activeRequest !== 'coords' && (
                        <Input placeholder="Название города" value={params.city}
                               onChange={(e: any) => setParams({...params, city: e.target.value})}/>
                    )}

                    {activeRequest === 'coords' && (
                        <>
                            <Input placeholder="Широта (lat)" value={params.lat}
                                   onChange={(e: any) => setParams({...params, lat: e.target.value})}/>
                            <Input placeholder="Долгота (lon)" value={params.lon}
                                   onChange={(e: any) => setParams({...params, lon: e.target.value})}/>
                        </>
                    )}

                    {(activeRequest === 'forecast' || activeRequest === 'filter') && (
                        <Input placeholder="Количество дней" value={params.days}
                               onChange={(e: any) => setParams({...params, days: e.target.value})}/>
                    )}

                    {activeRequest === 'filter' && (
                        <FilterPicker onSelect={(val) => setParams({...params, filter: val})}/>
                    )}

                    {(activeRequest === 'hourly') && (
                        <CalendarPicker onSelect={(d) => setParams({...params, date: d})}/>
                    )}

                    {activeRequest === 'history' && (
                        <CalendarPicker
                            onSelect={(d) => setParams({...params, date: d, start: '', end: ''})}
                            onRangeSelect={(start, end) => setParams({...params, start, end, date: ''})}
                        />
                    )}

                    <Button text="Отправить запрос" onClick={handleRequest} isLoading={isLoading}/>

                    {result && <div style={{
                        marginTop: '20px',
                        padding: '15px',
                        background: '#f8f9fa',
                        borderRadius: '8px',
                        overflowX: 'auto'
                    }}>
                        <pre style={{margin: 0}}>{JSON.stringify(result, null, 2)}</pre>
                    </div>}
                </div>
            )}
        </div>
    );
};