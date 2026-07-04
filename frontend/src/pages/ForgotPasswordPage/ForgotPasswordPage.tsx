import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../../api/authService.ts';
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {usePasswordVisibility} from '../../hooks/usePasswordVisibility.ts';
import {useApiAction} from '../../hooks/useApiAction.ts';
import {useNotification} from '../../hooks/useNotification.ts';
import styles from './Styles.module.css';
import '../../App.css';

export const ForgotPasswordPage = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');

    const pass = usePasswordVisibility();
    const conf = usePasswordVisibility();
    const {notify, setNotify, clearNotify} = useNotification();
    const {execute, isLoading} = useApiAction();

    const handleSendCode = async () => {
        const result = await execute(async () => {
            await authApi.forgotPassword(email);
        }, "Код отправлен на почту");

        if (result) {
            setStep(2);
        } else {
            setNotify("Ошибка отправки кода");
        }
    };

    const handleReset = async () => {
        if (password !== confirm) {
            setNotify("Пароли не совпадают");
            return;
        }

        const result = await execute(async () => {
            await authApi.resetPassword(email, code, password);
        }, "Пароль успешно изменен! Перенаправляем...");

        if (result) {
            setTimeout(() => navigate('/login'), 2000);
        } else {
            setNotify("Ошибка смены пароля");
        }
    };

    return (
        <div className={styles.container}>
            {notify && <Notification message={notify} onClose={clearNotify}/>}

            {step === 1 && (
                <>
                    <h1 className={styles.title}>Восстановление пароля</h1>
                    <Input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)}/>
                    <div className={styles.buttonWrapper}>
                        <Button text="Получить код" onClick={handleSendCode} isLoading={isLoading}/>
                    </div>
                </>
            )}

            {step === 2 && (
                <>
                    <h1 className={styles.title}>Введите данные</h1>
                    <Input placeholder="Код из письма" value={code} onChange={(e) => setCode(e.target.value)}/>
                    <Input
                        type={pass.show ? "text" : "password"}
                        placeholder="Новый пароль"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        rightIcon={
                            <button type="button" onClick={pass.toggle} className={styles.iconButton}>
                                {pass.show ? <FaEyeSlash/> : <FaEye/>}
                            </button>
                        }
                    />
                    <Input
                        type={conf.show ? "text" : "password"}
                        placeholder="Повтор пароля"
                        value={confirm}
                        onChange={(e) => setConfirm(e.target.value)}
                        rightIcon={
                            <button type="button" onClick={conf.toggle} className={styles.iconButton}>
                                {conf.show ? <FaEyeSlash/> : <FaEye/>}
                            </button>
                        }
                    />
                    <div className={styles.buttonWrapper}>
                        <Button text="Сменить пароль" onClick={handleReset} isLoading={isLoading}/>
                    </div>
                </>
            )}
        </div>
    );
};