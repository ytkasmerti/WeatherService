import {useState} from 'react';
import {useForm, Controller} from 'react-hook-form';
import {yupResolver} from '@hookform/resolvers/yup';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../../../api/authService.ts';
import {loginSchema} from './schema.ts';
import {Input} from '../Input/Input.tsx';
import {Button} from '../Button/Button.tsx';
import {Notification} from '../Notification/Notification.tsx';
import {usePasswordVisibility} from '../../../hooks/usePasswordVisibility.ts';
import {useNotification} from '../../../hooks/useNotification.ts';
import styles from './Styles.module.css';

interface LoginFormProps {
    onLoginSuccess: () => void;
    onForgotPassword: () => void;
    onGoToRegister: () => void;
}

export const LoginForm = ({onLoginSuccess, onForgotPassword, onGoToRegister}: LoginFormProps) => {
    const {control, handleSubmit} = useForm({
        resolver: yupResolver(loginSchema),
        defaultValues: {email: '', password: ''}
    });
    const [isLoading, setIsLoading] = useState(false);
    const {show, toggle} = usePasswordVisibility();
    const {notify, setNotify, clearNotify} = useNotification();

    const handleLogin = async (data: any) => {
        setIsLoading(true);
        try {
            const response = await authApi.login(data.email, data.password);
            localStorage.setItem('token', response.token);
            window.dispatchEvent(new Event('authChange'));
            setNotify("Вход выполнен!");
            setTimeout(onLoginSuccess, 1000);
        } catch (e: any) {
            setNotify(e.message || "Ошибка входа");
        } finally {
            setIsLoading(false);
        }
    };

    const onError = (errors: any) => {
        const firstError = Object.values(errors)[0] as any;
        if (firstError?.message) {
            setNotify(firstError.message);
        }
    };

    return (
        <div className={styles.container}>
            {notify && <Notification message={notify} onClose={clearNotify}/>}
            <h2 className={styles.title}>Вход</h2>
            <Controller
                name="email"
                control={control}
                render={({field}) => <Input {...field} placeholder="Email"/>}
            />
            <Controller
                name="password"
                control={control}
                render={({field}) => (
                    <Input{...field}
                          type={show ? "text" : "password"}
                          placeholder="Пароль"
                          rightIcon={
                              <button type="button" className={styles.iconButton} onClick={toggle}>
                                  {show ? <FaEyeSlash/> : <FaEye/>}
                              </button>
                          }
                    />
                )}
            />
            <div className={styles.actions}>
                <Button text="Войти" onClick={handleSubmit(handleLogin, onError)} isLoading={isLoading}/>
                <Button text="Забыли пароль?" onClick={onForgotPassword}/>
                <Button text="Регистрация" onClick={onGoToRegister}/>
            </div>
        </div>
    );
};