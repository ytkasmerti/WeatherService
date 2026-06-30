import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '../api/authService';

export const RegisterPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const navigate = useNavigate();

    const handleReg = async () => {
        if (password !== confirm) return toast.error("Пароли не совпадают");
        try {
            await authApi.register(email, password, confirm);
            toast.success("Регистрация успешна!");
            navigate('/login');
        } catch (e: any) { toast.error(e.message); }
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', width: '300px', margin: '50px auto' }}>
            <h1 style={{
                width: '100%',
                margin: '10px 0',
                lineHeight: '1.2',
                fontSize: '40px',
                textAlign: 'center'
            }}>
                Регистрация
            </h1>
            <input placeholder="Email" onChange={(e) => setEmail(e.target.value)} />
            <input type="password" placeholder="Пароль" onChange={(e) => setPassword(e.target.value)} />
            <input type="password" placeholder="Повтор пароля" onChange={(e) => setConfirm(e.target.value)} />
            <button onClick={handleReg}>Зарегистрироваться</button>
            <button onClick={() => navigate('/login')}>Назад</button>
        </div>
    );
};