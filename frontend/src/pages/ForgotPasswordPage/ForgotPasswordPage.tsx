import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useForm, Controller} from 'react-hook-form';
import {yupResolver} from '@hookform/resolvers/yup';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../../api/authApi.ts';
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {usePasswordVisibility} from '../../hooks/usePasswordVisibility.ts';
import {useNotification} from '../../hooks/useNotification.ts';
import {forgotPasswordSchema} from './schema.ts';
import styles from './styles.module.css';
import '../../App.css';

export const ForgotPasswordPage = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [isLoading, setIsLoading] = useState(false);
    const {control, getValues, trigger, formState: {errors}} = useForm({
        resolver: yupResolver(forgotPasswordSchema),
        defaultValues: {email: '', code: '', password: '', confirm: ''}
    });

    const pass = usePasswordVisibility();
    const conf = usePasswordVisibility();
    const {notify, setNotify, clearNotify} = useNotification();
    const showValidationError = () => {
        const firstError = Object.values(errors)[0];
        if (firstError?.message) {
            setNotify(firstError.message as string);
        }
    };

    const handleSendCode = async () => {
        const isEmailValid = await trigger('email');
        if (!isEmailValid) {
            showValidationError();
            return;
        }
        const email = getValues('email');
        setIsLoading(true);
        try {
            await authApi.forgotPassword(email);
            setNotify("Код отправлен на почту");
            setStep(2);
        } catch (err: any) {
            setNotify(err.message || "Ошибка отправки кода");
        } finally {
            setIsLoading(false);
        }
    };

    const handleReset = async () => {
        const isValid = await trigger(['email', 'code', 'password', 'confirm']);
        if (!isValid) {
            showValidationError();
            return;
        }
        const {email, code, password} = getValues();
        setIsLoading(true);
        try {
            await authApi.resetPassword(email, code, password);
            setNotify("Пароль успешно изменен!");
            setTimeout(() => navigate('/login'), 2000);
        } catch (err: any) {
            setNotify(err.message || "Ошибка смены пароля");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.headerWrapper}>
                <Button text="← Назад" onClick={() => step === 2 ? setStep(1) : navigate('/login')}/>
            </div>
            {notify && <Notification message={notify} onClose={clearNotify}/>}
            {step === 1 && (
                <>
                    <h1 className={styles.title}>Восстановление пароля</h1>
                    <Controller
                        name="email"
                        control={control}
                        render={({field}) => <Input {...field} placeholder="Email"/>}
                    />
                    <div className={styles.buttonWrapper}>
                        <Button text="Получить код" onClick={handleSendCode} isLoading={isLoading}/>
                    </div>
                </>
            )}
            {step === 2 && (
                <>
                    <h1 className={styles.title}>Введите данные</h1>
                    <Controller
                        name="code"
                        control={control}
                        render={({field}) => <Input {...field} placeholder="Код из письма"/>}
                    />
                    <Controller
                        name="password"
                        control={control}
                        render={({field}) => (
                            <Input
                                {...field}
                                type={pass.show ? "text" : "password"}
                                placeholder="Новый пароль"
                                rightIcon={
                                    <button type="button" onClick={pass.toggle} className={styles.iconButton}>
                                        {pass.show ? <FaEyeSlash/> : <FaEye/>}
                                    </button>
                                }
                            />
                        )}
                    />
                    <Controller
                        name="confirm"
                        control={control}
                        render={({field}) => (
                            <Input
                                {...field}
                                type={conf.show ? "text" : "password"}
                                placeholder="Повтор пароля"
                                rightIcon={
                                    <button type="button" onClick={conf.toggle} className={styles.iconButton}>
                                        {conf.show ? <FaEyeSlash/> : <FaEye/>}
                                    </button>
                                }
                            />
                        )}
                    />
                    <div className={styles.buttonWrapper}>
                        <Button text="Сменить пароль" onClick={handleReset} isLoading={isLoading}/>
                    </div>
                </>
            )}
        </div>
    );
};