import {useNavigate} from 'react-router-dom';
import {Button} from "../Button/Button.tsx";
import {useAuth} from '../../../hooks/useAuth.ts';
import styles from './Styles.module.css';

export const Header = () => {
    const navigate = useNavigate();
    const {isAuth} = useAuth();

    return (
        <header className={styles.header}>
            <h2 className={styles.title} onClick={() => navigate('/')}>
                WeatherService
            </h2>

            <div className={styles.navGroup}>
                {isAuth ? (
                    <>
                        <Button text="Прогноз погоды" onClick={() => navigate('/weather')}/>
                        <Button text="Подписки на предупреждения" onClick={() => navigate('/subscriptions')}/>
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