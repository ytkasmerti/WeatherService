import { useState, useEffect } from 'react';
import { Button } from '../components/Button/Button';
import { Input } from '../components/Input/Input';
import { Notification } from '../components/Notification/Notification';
import { Switcher } from '../components/Switcher/Switcher';
import { subscriptionApi } from '../api/authService';

export const SubscriptionPage = () => {
    const [city, setCity] = useState('');
    const [subs, setSubs] = useState<any[]>([]);
    const [notify, setNotify] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

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

    const handleSubscribe = async () => {
        if (!city.trim()) return;
        setIsLoading(true);
        try {
            const message = await subscriptionApi.subscribe(city, true, true, true, true);
            setNotify(message);
            setCity('');
            await loadSubs();
        } catch (e: any) {
            let errorMessage = e.message;
            try {
                const parsed = JSON.parse(e.message);
                errorMessage = parsed.message || parsed.error || e.message;
            } catch (err) {}
            if (errorMessage.toLowerCase().includes("limit") || errorMessage.toLowerCase().includes("лимит")) {
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
        <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            <h1>Подписки на погодные предупреждения</h1>

            <div style={{ padding: '15px', backgroundColor: '#e7f3ff', borderRadius: '8px', marginBottom: '20px', fontSize: '14px', color: '#0056b3', border: '1px solid #cce5ff' }}>
                <b>Как это работает:</b> Мы проверяем погоду в ваших городах ежедневно в <b>08:00</b>. Если условия соответствуют настройкам, вы получите письмо на email.
            </div>

            <div style={{ display: 'flex', gap: '10px', marginBottom: '30px', alignItems: 'center' }}>
                <div style={{ flex: 1 }}>
                    <Input placeholder="Название города" value={city} onChange={(e: any) => setCity(e.target.value)} />
                </div>
                <Button text="Подписаться" onClick={handleSubscribe} isLoading={isLoading} />
            </div>

            <div>
                <h3>Ваши подписки:</h3>
                {subs.length === 0 ? <p>У вас пока нет активных подписок.</p> : null}

                {subs.map((s: any) => (
                    <div key={s.id} style={{ padding: '15px', border: '1px solid #eee', borderRadius: '8px', marginBottom: '15px', backgroundColor: '#f9f9f9' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                            <span style={{ fontSize: '18px', fontWeight: 600 }}>{s.city}</span>
                            <Button text="Удалить" onClick={() => handleUnsubscribe(s.id)} />
                        </div>

                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '15px', fontSize: '14px' }}>
                            {[
                                { label: 'Жара', key: 'notifyHeat', val: s.notifyHeat },
                                { label: 'Холод', key: 'notifyCold', val: s.notifyCold },
                                { label: 'Ветер', key: 'notifyWind', val: s.notifyWind },
                                { label: 'Осадки', key: 'notifyPrecipitation', val: s.notifyPrecipitation }
                            ].map(item => (
                                <div key={item.key} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
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

            <div style={{ marginTop: '50px', padding: '20px', borderTop: '2px solid #eee', color: '#666' }}>
                <h4>Как пользоваться подписками:</h4>
                <ul style={{ paddingLeft: '20px', lineHeight: '1.8' }}>
                    <li>Введите полное название города, чтобы начать отслеживание.</li>
                    <li>Используйте переключатели, чтобы выбрать, о каких именно изменениях погоды вас уведомлять.</li>
                    <li>Все изменения сохраняются автоматически при переключении ползунка.</li>
                    <li>Если вы достигли лимита подписок, удалите ненужные или повысьте тариф до более высокого уровня, чтобы добавить новые города, </li>
                </ul>
            </div>
        </div>
    );
};