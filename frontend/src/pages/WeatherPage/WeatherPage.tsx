import {useState} from 'react';
import {useForm, Controller} from 'react-hook-form';
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {CalendarPicker} from '../../components/widgets/CalendarPicker/CalendarPicker.tsx';
import {FilterPicker} from '../../components/widgets/FilterPicker/FilterPicker.tsx';
import {menuMapping, type RequestType} from '../../constants/weatherRequests.ts';
import {fetchWeatherData} from '../../utils/weather.ts';
import {weatherSchema} from './schema.ts';
import styles from './Styles.module.css';
import '../../App.css';
import locationIcon from '../../assets/ui/location.svg';
import calendarIcon from '../../assets/ui/calendar.svg';

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
    const [result, setResult] = useState<any>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const {control, getValues, setValue, reset} = useForm<WeatherParams>({
        defaultValues: {city: '', lat: '', lon: '', date: '', start: '', end: '', days: '7', filter: ''}
    });

    const handleRequest = async () => {
        try {
            const params = getValues();
            await weatherSchema.validate(params, {
                context: {type: activeRequest}
            });
            setIsLoading(true);
            setError(null);
            const data = await fetchWeatherData(activeRequest, params);
            setResult(data);
        } catch (e: any) {
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
                        <Button text="← Назад"
                                onClick={() => {
                                    setActiveRequest(null);
                                    setResult(null);
                                    setError(null);
                                    reset();
                                }}
                        />
                    </div>

                    {activeRequest !== 'coords' && (
                        <div className={styles.inputWrapper}>
                            <img src={locationIcon} className={styles.inputIcon} alt="locationIcon"/>
                            <Controller name="city" control={control} render={({field}) => (
                                <Input placeholder="Название города" value={field.value} onChange={field.onChange}/>
                            )}/>
                        </div>
                    )}

                    {activeRequest === 'coords' && (
                        <>
                            <div className={styles.inputWrapper}>
                                <img src={locationIcon} className={styles.inputIcon} alt="locationIcon"/>
                                <Controller
                                    name="lat"
                                    control={control}
                                    render={({field}) => (
                                        <Input placeholder="Широта (lat)" value={field.value}
                                               onChange={field.onChange}/>
                                    )}
                                />
                            </div>
                            <div className={styles.inputWrapper}>
                                <img src={locationIcon} className={styles.inputIcon} alt="locationIcon"/>
                                <Controller
                                    name="lon"
                                    control={control}
                                    render={({field}) => (
                                        <Input placeholder="Долгота (lon)" value={field.value}
                                               onChange={field.onChange}/>
                                    )}
                                />
                            </div>
                        </>
                    )}

                    {(activeRequest === 'forecast' || activeRequest === 'filter') && (
                        <div className={styles.inputWrapper}>
                            <img src={calendarIcon} className={styles.inputIcon} alt="calendarIcon"/>
                            <Controller
                                name="days"
                                control={control}
                                render={({field}) => (
                                    <Input placeholder="Количество дней" value={field.value} onChange={field.onChange}/>
                                )}
                            />
                        </div>
                    )}

                    {activeRequest === 'filter' && (
                        <Controller
                            name="filter"
                            control={control}
                            render={({field}) => (
                                <FilterPicker value={field.value} onSelect={field.onChange}/>
                            )}
                        />
                    )}

                    {activeRequest === 'hourly' && (
                        <div className={styles.calendarWrapper}>
                            <CalendarPicker onSelect={(d) => setValue('date', d)}/>
                        </div>
                    )}

                    {activeRequest === 'history' && (
                        <div className={styles.calendarWrapper}>
                            <CalendarPicker
                                onSelect={(d) => {
                                    setValue('date', d);
                                    setValue('start', '');
                                    setValue('end', '');
                                }}
                                onRangeSelect={(start, end) => {
                                    setValue('start', start);
                                    setValue('end', end);
                                    setValue('date', '');
                                }}
                            />
                        </div>
                    )}
                    <Button text="Отправить запрос" onClick={handleRequest} isLoading={isLoading}/>
                    {result && (
                        <div className={styles.resultBox}>
                            <pre className={styles.preResult}>
                                {JSON.stringify(result, null, 2)}
                            </pre>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};