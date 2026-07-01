import {useNavigate} from 'react-router-dom';
import {Button} from "./Button/Button.tsx";

export const Header = () => {
    const navigate = useNavigate();
    return (
        <header style={{
            display: 'flex',
            justifyContent: 'space-between',
            padding: '15px 50px',
            borderBottom: '1px solid #ddd'
        }}>
            <h2 style={{cursor: 'pointer', margin: 0}} onClick={() => navigate('/')}>WeatherService</h2>
            <div style={{display: 'flex', gap: '15px'}}>
                <Button text="Войти" onClick={() => navigate('/login')}></Button>
                <Button text="Регистрация" onClick={() => navigate('/register')}></Button>
            </div>
        </header>
    );
};