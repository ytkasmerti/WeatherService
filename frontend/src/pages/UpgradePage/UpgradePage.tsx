import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Controller, useForm} from 'react-hook-form';
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {paymentApi} from '../../api/authService.ts';
import {SubscriptionPicker} from "../../components/widgets/SubscriptionPicker/SubscriptionPicker.tsx";
import {useNotification} from '../../hooks/useNotification.ts';
import {useAuth} from '../../hooks/useAuth';
import {upgradeSchema} from './schema.ts';
import styles from './Styles.module.css';
import '../../App.css';

interface UpgradeForm {
    cardNumber: string;
    expiry: string;
    cvc: string;
}

export const UpgradePage = () => {
    const navigate = useNavigate();
    const [level, setLevel] = useState('BASIC');
    const [isLoading, setIsLoading] = useState(false);
    const {notify, setNotify, clearNotify} = useNotification();
    const {logout} = useAuth();
    const {control, handleSubmit, getValues, reset} = useForm<UpgradeForm>({
        defaultValues: {cardNumber: '', expiry: '', cvc: ''}
    });

    const handleUpgrade = async () => {
        try {
            const {cardNumber, expiry, cvc} = getValues();
            await upgradeSchema.validate({cardNumber, expiry, cvc});
            setIsLoading(true);
            const payment = await paymentApi.createPayment(level);
            const result = await paymentApi.confirmPayment(payment.paymentId);
            if (result && (result.status === 'CONFIRMED' || result.status === 'SUCCESS')) {
                setNotify("Оплата прошла успешно! Пожалуйста, перезайдите в аккаунт.");
                reset();
                await logout();
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            } else {
                throw new Error(result.message || "Оплата отклонена банком. Попробуйте снова или повторите позднее.");
            }
        } catch (e: any) {
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
                <Controller
                    name="cardNumber"
                    control={control}
                    render={({field}) => (
                        <Input placeholder="Номер карты" value={field.value} onChange={field.onChange}/>
                    )}
                />

                <div className={styles.row}>
                    <div className={styles.rowItem}>
                        <Controller
                            name="expiry"
                            control={control}
                            render={({field}) => (
                                <Input placeholder="ММ/ГГ" value={field.value} onChange={field.onChange}/>
                            )}
                        />
                    </div>

                    <div className={styles.rowItem}>
                        <Controller
                            name="cvc"
                            control={control}
                            render={({field}) => (
                                <Input placeholder="CVC" value={field.value} onChange={field.onChange}/>
                            )}
                        />
                    </div>
                </div>
            </div>

            <div className={styles.buttonWrapper}>
                <Button text="Подтвердить оплату" onClick={handleSubmit(handleUpgrade)} isLoading={isLoading}/>
            </div>
        </div>
    );
};