import {useState} from 'react';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {authApi} from '../../api/authService';
import {Input} from '../Input/Input';
import {Button} from '../Button/Button';
import {Notification} from '../Notification/Notification'; // Импортируй свой новый компонент
import styles from './RegisterForm.module.css';

interface RegisterFormProps {
    onCancel: () => void;
    onSuccess: () => void;
}

export const RegisterForm = ({onCancel, onSuccess}: RegisterFormProps) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [showPass, setShowPass] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [notify, setNotify] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const renderEyeButton = (show: boolean, setShow: (val: boolean) => void) => (
        <button
            type="button"
            onClick={() => setShow(!show)}
            style={{background: 'none', border: 'none', cursor: 'pointer', display: 'flex'}}
        >
            {show ? <FaEyeSlash/> : <FaEye/>}
        </button>
    );

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
        }
    };

    return (
        <div className={styles.container}>
            {notify && (
                <Notification message={notify} onClose={() => setNotify(null)}/>
            )}

            <h2 className={styles.title}>Регистрация</h2>

            <Input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)}/>

            <Input
                type={showPass ? "text" : "password"}
                placeholder="Пароль"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                rightIcon={renderEyeButton(showPass, setShowPass)}
            />

            <Input
                type={showConfirm ? "text" : "password"}
                placeholder="Повтор пароля"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                rightIcon={renderEyeButton(showConfirm, setShowConfirm)}
            />

            <div className={styles.actions}>
                <Button text="Назад" onClick={onCancel}/>
                <Button text="Зарегистрироваться" onClick={handleRegister} isLoading={isLoading}/>
            </div>
        </div>
    );
};