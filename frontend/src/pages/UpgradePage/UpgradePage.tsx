import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button/Button.tsx';
import { Input } from '../../components/ui/Input/Input.tsx';
import { Notification } from '../../components/ui/Notification/Notification.tsx';
import { paymentApi } from '../../api/authService.ts';
import { SubscriptionPicker } from "../../components/ui/SubscriptionPicker/SubscriptionPicker.tsx";
import { useNotification } from '../../hooks/useNotification.ts';
import { upgradeSchema } from './schema.ts';
import styles from './Styles.module.css';
import '../../App.css';

export const UpgradePage = () => {
    const navigate = useNavigate();
    const [level, setLevel] = useState('BASIC');
    const [isLoading, setIsLoading] = useState(false);
    const [cardNumber, setCardNumber] = useState('');
    const [expiry, setExpiry] = useState('');
    const [cvc, setCvc] = useState('');
    const { notify, setNotify, clearNotify } = useNotification();

    const handleUpgrade = async () => {
        try {
            // 1. Валидация полей карты
            await upgradeSchema.validate({ cardNumber, expiry, cvc });
            setIsLoading(true);

            // 2. Инициализация платежа на бэкенде
            const payment = await paymentApi.createPayment(level);

            // 3. Подтверждение платежа
            const result = await paymentApi.confirmPayment(payment.paymentId);

            // 4. Проверка статуса (с учетом вашего Enum PaymentStatus)
            // Успех, если статус CONFIRMED
            if (result && (result.status === 'CONFIRMED' || result.status === 'SUCCESS')) {
                setNotify("Оплата прошла успешно! Перенаправляем на страницу входа...");

                // Удаляем данные авторизации
                localStorage.removeItem('token');
                window.dispatchEvent(new Event('authChange'));

                // Задержка 2 секунды перед редиректом, чтобы пользователь увидел сообщение
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            } else {
                // Если статус FAILED, EXPIRED или другой — выбрасываем ошибку
                throw new Error(result.message || "Оплата отклонена банком. Попробуйте снова.");
            }

        } catch (e: any) {
            // Обработка ошибок валидации или ошибок от API
            const errorMsg = e.errors ? e.errors[0] : (e.message || "Ошибка при оплате. Попробуйте позднее.");
            setNotify(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            {notify && <Notification message={notify} onClose={clearNotify}/>}

            <div className={styles.backButtonWrapper}>
                <Button text="← Назад в профиль" onClick={() => navigate('/profile')}/>
            </div>

            <h1>Повышение тарифа</h1>

            <div className={styles.section}>
                <label className={styles.label}>Выберите тариф:</label>
                <div className={styles.pickerWrapper}>
                    <SubscriptionPicker value={level} onSelect={setLevel}/>
                </div>
            </div>

            <div className={styles.inputGroup}>
                <Input placeholder="Номер карты" value={cardNumber} onChange={(e) => setCardNumber(e.target.value)}/>

                <div className={styles.row}>
                    <div className={styles.rowItem}>
                        <Input placeholder="ММ/ГГ" value={expiry} onChange={(e) => setExpiry(e.target.value)}/>
                    </div>
                    <div className={styles.rowItem}>
                        <Input placeholder="CVC" value={cvc} onChange={(e) => setCvc(e.target.value)}/>
                    </div>
                </div>
            </div>

            <div className={styles.buttonWrapper}>
                <Button
                    text="Подтвердить оплату"
                    onClick={handleUpgrade}
                    isLoading={isLoading}
                />
            </div>
        </div>
    );
};