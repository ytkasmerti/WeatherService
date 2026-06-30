import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '../api/authService';

export const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [show, setShow] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async () => {
        try {
            await authApi.login(email, password);
            toast.success("Вход выполнен!");
            navigate('/success');
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
                Вход
            </h1>
            <input placeholder="Email" onChange={(e) => setEmail(e.target.value)} />
            <div style={{ display: 'flex', gap: '5px' }}>
                <input type={show ? "text" : "password"} placeholder="Пароль" onChange={(e) => setPassword(e.target.value)} style={{ flex: 1 }} />
                <button onClick={() => setShow(!show)}>{show ? "👁" : "🙈"}</button>
            </div>
            <button onClick={handleLogin}>Войти</button>
            <button onClick={() => navigate('/forgot-password')}>Забыли пароль?</button>
            <button onClick={() => navigate('/register')}>Регистрация</button>
        </div>
    );
};