import {useState} from 'react';
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {CalendarPicker} from '../../components/ui/CalendarPicker/CalendarPicker.tsx';
import {FilterPicker} from '../../components/ui/FilterPicker/FilterPicker.tsx';
import {menuMapping, type RequestType} from '../../constants/weatherRequests.ts';
import {fetchWeatherData} from '../../utils/weather.ts';
import {weatherSchema} from './schema.ts';
import styles from './Styles.module.css';
import '../../App.css';

interface WeatherParams {
    city: string;
    lat: string;
    lon: string;
    date: string;
    start: string;
    end: string;
    days: string;
    filter: string;
}

export const WeatherPage = () => {
    const [activeRequest, setActiveRequest] = useState<RequestType | null>(null);
    const [params, setParams] = useState<WeatherParams>({
        city: '', lat: '', lon: '', date: '', start: '', end: '', days: '7', filter: ''
    });
    const [result, setResult] = useState<any>(null);

    // Заменяем useApiAction на локальные стейты
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleRequest = async () => {
        try {
            // Валидация
            await weatherSchema.validate(params, {context: {type: activeRequest}});

            setIsLoading(true);
            setError(null);

            const data = await fetchWeatherData(activeRequest, params);
            setResult(data);
        } catch (e: any) {
            // Если ошибка валидации yup, берем первую ошибку
            if (e.name === 'ValidationError') {
                setError(e.errors[0]);
            } else {
                setError(e.message || "Ошибка при выполнении запроса");
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            {error && <Notification message={error} onClose={() => setError(null)}/>}

            <h1 className={styles.title}>WeatherService</h1>

            {!activeRequest ? (
                <div className={styles.grid}>
                    {(Object.keys(menuMapping) as RequestType[]).map(type => (
                        <div key={type} className={styles.card}>
                            <Button text={menuMapping[type].title} onClick={() => setActiveRequest(type)}/>
                            <div>
                                <p className={styles.cardDesc}>{menuMapping[type].desc}</p>
                                <p className={styles.cardLevel}>Уровень: {menuMapping[type].level}</p>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className={styles.formContainer}>
                    <div className={styles.backButton}>
                        <Button text="← Назад" onClick={() => {
                            setActiveRequest(null);
                            setResult(null);
                            setError(null); // Очищаем ошибку при выходе
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
                        <div className={styles.calendarWrapper}>
                            <CalendarPicker onSelect={(d) => setParams({...params, date: d})}/>
                        </div>
                    )}

                    {activeRequest === 'history' && (
                        <div className={styles.calendarWrapper}>
                            <CalendarPicker
                                onSelect={(d) => setParams({...params, date: d, start: '', end: ''})}
                                onRangeSelect={(start, end) => setParams({...params, start, end, date: ''})}
                            />
                        </div>
                    )}

                    <Button text="Отправить запрос" onClick={handleRequest} isLoading={isLoading}/>

                    {result && (
                        <div className={styles.resultBox}>
                            <pre className={styles.preResult}>{JSON.stringify(result, null, 2)}</pre>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};