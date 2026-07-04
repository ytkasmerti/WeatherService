import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {paymentApi} from '../../api/authService.ts';
import {SubscriptionPicker} from "../../components/ui/SubscriptionPicker/SubscriptionPicker.tsx";
import {useNotification} from '../../hooks/useNotification.ts';
import styles from './Styles.module.css';
import '../../App.css';

export const UpgradePage = () => {
    const navigate = useNavigate();
    const [level, setLevel] = useState('BASIC');
    const [isLoading, setIsLoading] = useState(false);
    const {notify, setNotify, clearNotify} = useNotification();

    const handleUpgrade = async () => {
        setIsLoading(true);
        try {
            const payment = await paymentApi.createPayment(level);
            await paymentApi.confirmPayment(payment.paymentId);

            setNotify("Оплата прошла успешно! Пожалуйста, войдите снова.");
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authChange'));

            setTimeout(() => navigate('/login'), 2000);
        } catch (e: any) {
            setNotify(e.message || "Ошибка при оплате");
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
                <Input placeholder="Номер карты"/>

                <div className={styles.row}>
                    <div className={styles.rowItem}>
                        <Input placeholder="ММ/ГГ"/>
                    </div>
                    <div className={styles.rowItem}>
                        <Input placeholder="CVC"/>
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