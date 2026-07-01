import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../api/authService';
import {Button} from '../components/Button/Button';
import {Input} from '../components/Input/Input';
import {Notification} from '../components/Notification/Notification';
import '../App.css';

export const ForgotPasswordPage = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [showPass, setShowPass] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [notify, setNotify] = useState<string | null>(null);

    const handleSendCode = async () => {
        setIsLoading(true);
        try {
            await authApi.forgotPassword(email);
            setStep(2);
            setNotify("Код отправлен на почту");
        } catch (e: any) {
            setNotify(e.message || "Ошибка отправки");
        } finally {
            setIsLoading(false);
        }
    };

    const handleReset = async () => {
        if (code.length !== 6) {
            setNotify("Код должен состоять из 6 цифр");
            return;
        }
        if (password !== confirm) {
            setNotify("Пароли не совпадают");
            return;
        }

        setIsLoading(true);
        try {
            await authApi.resetPassword(email, code, password);
            setNotify("Пароль успешно изменен!");
            setTimeout(() => setStep(3), 1000);
        } catch (e: any) {
            setNotify(e.message || "Неверный код или ошибка");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '20px',
            width: '100%',
            maxWidth: '400px',
            margin: '50px auto',
        }}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            {step < 3 && (
                <div style={{width: '100%', display: 'flex', justifyContent: 'flex-start'}}>
                    <Button text="← Назад" onClick={() => navigate('/login')}/>
                </div>
            )}

            {step === 1 && (
                <>
                    <h1>Восстановление пароля</h1>
                    <Input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)}/>
                    <Button text="Получить код" onClick={handleSendCode} isLoading={isLoading}/>
                </>
            )}

            {step === 2 && (
                <>
                    <h1>Введите данные</h1>
                    <Input placeholder="Код из письма" value={code} onChange={(e) => setCode(e.target.value)}/>
                    <Input
                        type={showPass ? "text" : "password"}
                        placeholder="Новый пароль"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        rightIcon={
                            <button type="button" onClick={() => setShowPass(!showPass)}
                                    style={{background: 'none', border: 'none', cursor: 'pointer', display: 'flex'}}>
                                {showPass ? <FaEyeSlash/> : <FaEye/>}
                            </button>
                        }
                    />
                    <Input
                        type={showConfirm ? "text" : "password"}
                        placeholder="Повтор пароля"
                        value={confirm}
                        onChange={(e) => setConfirm(e.target.value)}
                        rightIcon={
                            <button type="button" onClick={() => setShowConfirm(!showConfirm)}
                                    style={{background: 'none', border: 'none', cursor: 'pointer', display: 'flex'}}>
                                {showConfirm ? <FaEyeSlash/> : <FaEye/>}
                            </button>
                        }
                    />
                    <Button text="Сменить пароль" onClick={handleReset} isLoading={isLoading}/>
                </>
            )}

            {step === 3 && (
                <>
                    <h1>Пароль успешно изменен!</h1>
                    <Button text="Вернуться ко входу" onClick={() => navigate('/login')}/>
                </>
            )}
        </div>
    );
}