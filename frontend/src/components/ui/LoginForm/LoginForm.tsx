import { useState } from 'react';
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { authApi } from '../../../api/authService.ts';
import { loginSchema } from './schema.ts';
import { Input } from '../Input/Input.tsx';
import { Button } from '../Button/Button.tsx';
import { Notification } from '../Notification/Notification.tsx';
import { usePasswordVisibility } from '../../../hooks/usePasswordVisibility.ts';
import { useNotification } from '../../../hooks/useNotification.ts';
import styles from './Styles.module.css';

interface LoginFormProps {
    onLoginSuccess: () => void;
    onForgotPassword: () => void;
    onGoToRegister: () => void;
}

export const LoginForm = ({ onLoginSuccess, onForgotPassword, onGoToRegister }: LoginFormProps) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const { show, toggle } = usePasswordVisibility();
    const { notify, setNotify, clearNotify } = useNotification();

    const handleLogin = async () => {
        try {
            // Валидация через схему
            await loginSchema.validate({ email, password });

            setIsLoading(true);
            const data = await authApi.login(email, password);
            localStorage.setItem('token', data.token);
            window.dispatchEvent(new Event('authChange'));

            setNotify("Вход выполнен!");
            setTimeout(onLoginSuccess, 1000);
        } catch (e: any) {
            // e.errors[0] будет содержать сообщение из схемы, если ошибка валидации
            setNotify(e.errors ? e.errors[0] : (e.message || "Ошибка входа"));
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            {notify && <Notification message={notify} onClose={clearNotify} />}

            <h2 className={styles.title}>Вход</h2>

            <Input
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
            />

            <Input
                type={show ? "text" : "password"}
                placeholder="Пароль"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                rightIcon={
                    <button type="button" className={styles.iconButton} onClick={toggle}>
                        {show ? <FaEyeSlash /> : <FaEye />}
                    </button>
                }
            />

            <div className={styles.actions}>
                <Button text="Войти" onClick={handleLogin} isLoading={isLoading} />
                <Button text="Забыли пароль?" onClick={onForgotPassword} />
                <Button text="Регистрация" onClick={onGoToRegister} />
            </div>
        </div>
    );
};