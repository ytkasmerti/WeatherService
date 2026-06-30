import { useNavigate } from 'react-router-dom';

export const Header = () => {
    const navigate = useNavigate();
    return (
        <header style={{ display: 'flex', justifyContent: 'space-between', padding: '15px 50px', borderBottom: '1px solid #ddd' }}>
            <h2 style={{ cursor: 'pointer', margin: 0 }} onClick={() => navigate('/')}>WeatherService</h2>
            <div style={{ display: 'flex', gap: '15px' }}>
                <button onClick={() => navigate('/login')}>Войти</button>
                <button onClick={() => navigate('/register')}>Регистрация</button>
            </div>
        </header>
    );
};