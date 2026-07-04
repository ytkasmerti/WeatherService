import {useState} from 'react';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../../../api/authService.ts';
import {Input} from '../Input/Input.tsx';
import {Button} from '../Button/Button.tsx';
import {Notification} from '../Notification/Notification.tsx';
import {usePasswordVisibility} from '../../../hooks/usePasswordVisibility.ts';
import {useNotification} from '../../../hooks/useNotification.ts';
import styles from './Styles.module.css';

interface RegisterFormProps {
    onCancel: () => void;
    onSuccess: () => void;
}

export const RegisterForm = ({onCancel, onSuccess}: RegisterFormProps) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const pass = usePasswordVisibility();
    const conf = usePasswordVisibility();
    const {notify, setNotify, clearNotify} = useNotification();

    const handleRegister = async () => {
        if (password !== confirm) {
            setNotify("Пароли не совпадают");
            return;
        }
        setIsLoading(true);
        try {
            await authApi.register(email, password, confirm);
            setNotify("Регистрация успешна!");
            setTimeout(() => {
                onSuccess();
            }, 2000);
        } catch (e: any) {
            setNotify(e.message || "Ошибка регистрации");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            {notify && (
                <Notification message={notify} onClose={clearNotify}/>
            )}

            <h2 className={styles.title}>Регистрация</h2>

            <Input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)}/>

            <Input
                type={pass.show ? "text" : "password"}
                placeholder="Пароль"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                rightIcon={
                    <button type="button" className={styles.iconButton} onClick={pass.toggle}>
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
                    <button type="button" className={styles.iconButton} onClick={conf.toggle}>
                        {conf.show ? <FaEyeSlash/> : <FaEye/>}
                    </button>
                }
            />

            <div className={styles.actions}>
                <Button text="Назад" onClick={onCancel}/>
                <Button text="Зарегистрироваться" onClick={handleRegister} isLoading={isLoading}/>
            </div>
        </div>
    );
};