import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Button} from '../components/Button/Button';
import {Input} from '../components/Input/Input';
import {Notification} from '../components/Notification/Notification';
import {paymentApi} from '../api/authService';
import {SubscriptionPicker} from "../components/SubscriptionPicker/SubscriptionPicker.tsx";

export const UpgradePage = () => {
    const navigate = useNavigate();
    const [level, setLevel] = useState('BASIC');
    const [isLoading, setIsLoading] = useState(false);
    const [notify, setNotify] = useState<string | null>(null);

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
            setNotify(e.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{padding: '20px', maxWidth: '400px', margin: '0 auto'}}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            <div style={{marginBottom: '20px'}}>
                <Button text="← Назад в профиль" onClick={() => navigate('/profile')}/>
            </div>

            <h1>Повышение тарифа</h1>

            <div style={{marginBottom: '20px'}}>
                <label style={{fontWeight: 'bold'}}>Выберите тариф:</label>
                <div style={{marginTop: '8px'}}>
                    <SubscriptionPicker value={level} onSelect={setLevel}/>
                </div>
            </div>

            <div style={{display: 'flex', flexDirection: 'column', gap: '10px'}}>
                <Input placeholder="Номер карты"/>

                <div style={{display: 'flex', gap: '10px'}}>
                    <div style={{flex: 1}}>
                        <Input placeholder="ММ/ГГ"/>
                    </div>
                    <div style={{flex: 1}}>
                        <Input placeholder="CVC"/>
                    </div>
                </div>
            </div>

            <div style={{marginTop: '20px', display: 'flex', justifyContent: 'center'}}>
                <Button
                    text="Подтвердить оплату"
                    onClick={handleUpgrade}
                    isLoading={isLoading}
                />
            </div>
        </div>
    );
};