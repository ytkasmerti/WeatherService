import {useState} from 'react';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../../api/authService';
import {Input} from '../Input/Input';
import {Button} from '../Button/Button';
import {Notification} from '../Notification/Notification';
import styles from './LoginForm.module.css';

interface LoginFormProps {
    onLoginSuccess: () => void;
    onForgotPassword: () => void;
    onGoToRegister: () => void;
}

export const LoginForm = ({onLoginSuccess, onForgotPassword, onGoToRegister}: LoginFormProps) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPass, setShowPass] = useState(false);
    const [notify, setNotify] = useState<string | null>(null);

    const handleLogin = async () => {
        try {
            const data = await authApi.login(email, password);
            localStorage.setItem('token', data.token);
            window.dispatchEvent(new Event('authChange'));

            setNotify("Вход выполнен!");
            setTimeout(onLoginSuccess, 1000);
        } catch (e: any) {
            setNotify(e.message || "Ошибка входа");
        }
    };

    return (
        <div className={styles.container}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            <h2 className={styles.title}>Вход</h2>

            <Input
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
            />

            <Input
                type={showPass ? "text" : "password"}
                placeholder="Пароль"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                rightIcon={
                    <button
                        type="button"
                        onClick={() => setShowPass(!showPass)}
                        style={{background: 'none', border: 'none', cursor: 'pointer', display: 'flex'}}
                    >
                        {showPass ? <FaEyeSlash/> : <FaEye/>}
                    </button>
                }
            />

            <div className={styles.actions}>
                <Button text="Войти" onClick={handleLogin}/>
                <Button text="Забыли пароль?" onClick={onForgotPassword}/>
                <Button text="Регистрация" onClick={onGoToRegister}/>
            </div>
        </div>
    );
};