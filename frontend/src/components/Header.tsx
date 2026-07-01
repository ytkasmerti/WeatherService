import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {Button} from "./Button/Button.tsx";

export const Header = () => {
    const navigate = useNavigate();
    const [isAuth, setIsAuth] = useState(!!localStorage.getItem('token'));

    useEffect(() => {
        const updateAuth = () => setIsAuth(!!localStorage.getItem('token'));
        window.addEventListener('authChange', updateAuth);
        return () => window.removeEventListener('authChange', updateAuth);
    }, []);

    return (
        <header style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '15px 50px',
            borderBottom: '1px solid #ddd',
            background: '#fff'
        }}>
            <h2 style={{cursor: 'pointer', margin: 0}} onClick={() => navigate('/')}>
                WeatherService
            </h2>

            <div style={{display: 'flex', gap: '10px'}}>
                {isAuth ? (
                    <>
                        <Button text="Прогноз погоды" onClick={() => navigate('/weather')}/>
                        <Button text="История платежей" onClick={() => alert('Заглушка: История')}/>
                        <Button text="Подписка" onClick={() => alert('Заглушка: Подписка')}/>
                        <Button text="Личный кабинет" onClick={() => navigate('/profile')}/>
                    </>
                ) : (
                    <>
                        <Button text="Войти" onClick={() => navigate('/login')}/>
                        <Button text="Регистрация" onClick={() => navigate('/register')}/>
                    </>
                )}
            </div>
        </header>
    );
};