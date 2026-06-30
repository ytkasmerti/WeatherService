import { useState } from 'react';
import toast from 'react-hot-toast';
import { authApi } from '../api/authService';

export const ForgotPasswordPage = () => {
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [show, setShow] = useState(false); // Состояние для показа пароля

    const handleSendCode = async () => {
        try {
            await authApi.forgotPassword(email);
            setStep(2);
            toast.success("Код отправлен на почту");
        } catch (e: any) {
            toast.error(e.message || "Ошибка отправки");
        }
    };

    const handleReset = async () => {
        if (code.length !== 6) return toast.error("Код должен состоять из 6 цифр");
        if (password !== confirm) return toast.error("Пароли не совпадают");

        try {
            await authApi.resetPassword(email, code, password);
            toast.success("Пароль успешно изменен!");
            setStep(3);
        } catch (e: any) {
            toast.error(e.message || "Неверный код или ошибка");
        }
    };

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center', // Центровка по горизонтали
            justifyContent: 'center', // Центровка по вертикали
            gap: '20px',
            width: '100%',
            maxWidth: '400px', // Ограничение ширины для красоты
            margin: '50px auto',
            textAlign: 'center' // Текст внутри блоков по центру
        }}>
            {step === 1 && (
                <>
                    <h1 style={{
                        width: '100%',
                        margin: '10px 0',
                        lineHeight: '1.2',
                        fontSize: '40px',
                        textAlign: 'center'
                    }}>
                        Восстановление пароля
                    </h1>
                    <input style={{ width: '100%', padding: '10px' }} placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
                    <button style={{ width: '100%', padding: '10px' }} onClick={handleSendCode}>Получить код</button>
                </>
            )}

            {step === 2 && (
                <>
                    <h1 style={{
                        width: '100%',
                        margin: '10px 0',
                        lineHeight: '1.2',
                        fontSize: '40px',
                        textAlign: 'center'
                    }}>
                        Введите данные
                    </h1>
                    <input style={{ width: '100%', padding: '10px' }} placeholder="Код из письма" value={code} onChange={(e) => setCode(e.target.value)} />

                    <div style={{ width: '100%', display: 'flex', gap: '5px' }}>
                        <input style={{ flex: 1, padding: '10px' }} type={show ? "text" : "password"} placeholder="Новый пароль" value={password} onChange={(e) => setPassword(e.target.value)} />
                        <button onClick={() => setShow(!show)}>{show ? "🙈" : "👁"}</button>
                    </div>

                    <input style={{ width: '100%', padding: '10px' }} type="password" placeholder="Повтор пароля" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
                    <button style={{ width: '100%', padding: '10px' }} onClick={handleReset}>Сменить пароль</button>
                </>
            )}

            {step === 3 && (
                <>
                    <h1 style={{
                        width: '100%',
                        margin: '10px 0',
                        lineHeight: '1.2',
                        fontSize: '40px',
                        textAlign: 'center'
                    }}>
                        Пароль успешно изменен!
                    </h1>
                    <a href="/login" style={{ fontSize: '18px' }}>Вернуться ко входу</a>
                </>
            )}
        </div>
    );
}