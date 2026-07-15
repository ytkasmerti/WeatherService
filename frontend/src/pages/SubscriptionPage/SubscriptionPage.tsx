import {useState, useEffect} from 'react';
import {useForm, Controller} from 'react-hook-form';
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {Switcher} from '../../components/ui/Switcher/Switcher.tsx';
import {subscriptionApi} from '../../api/subscriptionApi.ts';
import {useNotification} from '../../hooks/useNotification.ts';
import {subscriptionSchema} from './schema.ts';
import styles from './styles.module.css';
import '../../App.css';

interface SubscriptionForm {
    city: string;
}

export const SubscriptionPage = () => {
    const [subs, setSubs] = useState<any[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const {notify, setNotify, clearNotify} = useNotification();
    const {control, handleSubmit, reset} = useForm<SubscriptionForm>({
        defaultValues: {city: ''}
    });

    useEffect(() => {
        loadSubs();
    }, []);

    const loadSubs = async () => {
        try {
            const data = await subscriptionApi.getSubscriptions();
            setSubs(data);
        } catch (e: any) {
            setNotify("Ошибка загрузки подписок");
        }
    };

    const updateSubscription = async (city: string, params: any) => {
        try {
            await subscriptionApi.subscribe(city, params.heat, params.cold, params.wind, params.precip);
            await loadSubs();
        } catch (e: any) {
            setNotify("Ошибка при обновлении настроек");
        }
    };

    const handleSubscribe = async (data: SubscriptionForm) => {
        try {
            await subscriptionSchema.validate(data);
            setIsLoading(true);
            const message = await subscriptionApi.subscribe(data.city, true, true, true, true);
            setNotify(message);
            reset({city: ''});
            await loadSubs();

        } catch (e: any) {
            if (e.name === 'ValidationError') {
                setNotify(e.message);
                return;
            }
            let errorMessage = e.message;
            try {
                const parsed = JSON.parse(e.message);
                errorMessage = parsed.message || parsed.error || e.message;
            } catch {
            }
            if (errorMessage.toLowerCase().includes("limit") ||
                errorMessage.toLowerCase().includes("лимит")) {
                errorMessage = "Превышен лимит подписок на уведомления";
            }
            setNotify(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    const handleUnsubscribe = async (id: number) => {
        try {
            await subscriptionApi.unsubscribe(id);
            setSubs(prevSubs => prevSubs.filter(s => s.id !== id));
            setNotify("Подписка удалена");
        } catch (e: any) {
            setNotify("Ошибка при отписке");
        }
    };

    return (
        <div className={styles.container}>
            {notify && <Notification message={notify} onClose={clearNotify}/>}
            <h1>Подписки на погодные предупреждения</h1>
            <div className={styles.infoBox}>
                <b>Как это работает:</b> Мы проверяем погоду в ваших городах ежедневно в <b>08:00</b>.
                Если условия соответствуют настройкам, вы получите письмо на email.
            </div>

            <div className={styles.inputGroup}>
                <div className={styles.inputWrapper}>
                    <Controller
                        name="city"
                        control={control}
                        render={({field}) => (
                            <Input placeholder="Название города" {...field}/>
                        )}
                    />
                </div>
                <Button
                    text="Подписаться"
                    onClick={handleSubmit(handleSubscribe)}
                    isLoading={isLoading}
                />
            </div>

            <div>
                <h3>Ваши подписки:</h3>
                {subs.length === 0 && <p>У вас пока нет активных подписок.</p>}
                {subs.map((s: any) => (
                    <div key={s.id} className={styles.subscriptionCard}>
                        <div className={styles.cardHeader}>
                            <span className={styles.cityName}>{s.city}</span>
                            <Button text="Удалить" onClick={() => handleUnsubscribe(s.id)}/>
                        </div>
                        <div className={styles.switchersGroup}>
                            {[
                                {label: 'Жара', key: 'notifyHeat', val: s.notifyHeat},
                                {label: 'Холод', key: 'notifyCold', val: s.notifyCold},
                                {label: 'Ветер', key: 'notifyWind', val: s.notifyWind},
                                {label: 'Осадки', key: 'notifyPrecipitation', val: s.notifyPrecipitation}
                            ].map(item => (
                                <div key={item.key} className={styles.switcherItem}>
                                    <span>{item.label}</span>
                                    <Switcher
                                        isOn={item.val}
                                        size="small"
                                        onToggle={(v) => updateSubscription(s.city, {
                                            heat: item.key === 'notifyHeat' ? v : s.notifyHeat,
                                            cold: item.key === 'notifyCold' ? v : s.notifyCold,
                                            wind: item.key === 'notifyWind' ? v : s.notifyWind,
                                            precip: item.key === 'notifyPrecipitation' ? v : s.notifyPrecipitation
                                        })}
                                    />
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};