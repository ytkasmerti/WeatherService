import {useState} from 'react';
import {useForm, Controller} from 'react-hook-form';
import {yupResolver} from '@hookform/resolvers/yup';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../../../api/authService.ts';
import {registerSchema} from './schema.ts';
import {Input} from '../../ui/Input/Input.tsx';
import {Button} from '../../ui/Button/Button.tsx';
import {Notification} from '../../ui/Notification/Notification.tsx';
import {usePasswordVisibility} from '../../../hooks/usePasswordVisibility.ts';
import {useNotification} from '../../../hooks/useNotification.ts';
import styles from './Styles.module.css';

interface RegisterFormProps {
    onCancel: () => void;
    onSuccess: () => void;
}

export const RegisterForm = ({onCancel, onSuccess}: RegisterFormProps) => {
    const {control, handleSubmit} = useForm({
        resolver: yupResolver(registerSchema),
        defaultValues: {email: '', password: '', confirm: ''}
    });

    const [isLoading, setIsLoading] = useState(false);
    const pass = usePasswordVisibility();
    const conf = usePasswordVisibility();
    const {notify, setNotify, clearNotify} = useNotification();
    const handleRegister = async (data: any) => {
        setIsLoading(true);
        try {
            await authApi.register(data.email, data.password, data.confirm);
            setNotify("Регистрация успешна!");
            setTimeout(onSuccess, 2000);
        } catch (e: any) {
            setNotify(e.message || "Ошибка регистрации");
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
            <h2 className={styles.title}>Регистрация</h2>

            <Controller name="email" control={control} render={({field}) =>
                <Input {...field} placeholder="Email"/>}/>

            <Controller name="password" control={control} render={({field}) => (
                <Input {...field} type={pass.show ? "text" : "password"} placeholder="Пароль"
                       rightIcon={<button type="button" className={styles.iconButton} onClick={pass.toggle}>{pass.show ?
                           <FaEyeSlash/> : <FaEye/>}</button>}/>
            )}/>

            <Controller name="confirm" control={control} render={({field}) => (
                <Input {...field} type={conf.show ? "text" : "password"} placeholder="Повтор пароля"
                       rightIcon={<button type="button" className={styles.iconButton} onClick={conf.toggle}>{conf.show ?
                           <FaEyeSlash/> : <FaEye/>}</button>}/>
            )}/>

            <div className={styles.actions}>
                <Button text="Войти" onClick={onCancel}/>
                <Button text="Зарегистрироваться" onClick={handleSubmit(handleRegister, onError)} isLoading={isLoading}/>
            </div>
        </div>
    );
};