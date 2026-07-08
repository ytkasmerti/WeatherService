import { useNavigate } from 'react-router-dom';
import { Button } from "../Button/Button.tsx";
import { useAuth } from '../../../hooks/useAuth.ts';
import styles from './Styles.module.css';

interface HeaderProps {
    variant?: 'default' | 'minimal';
}

export const Header = ({ variant = 'default' }: HeaderProps) => {
    const navigate = useNavigate();
    const { isAuth } = useAuth();
    return (
        <header className={styles.header}>
            <h2 className={styles.title} onClick={() => navigate('/')}> WeatherService </h2>
            {variant !== 'minimal' && (
                <div className={styles.navGroup}>
                    <Button text="Войти" onClick={() => navigate('/login')} />
                    {isAuth ? (
                        <>
                            <Button text="Прогноз погоды" onClick={() => navigate('/weather')} />
                            <Button text="Подписки" onClick={() => navigate('/subscriptions')} />
                            <Button text="Личный кабинет" onClick={() => navigate('/profile')} />
                        </>
                    ) : (
                        <Button text="Регистрация" onClick={() => navigate('/register')} />
                    )}
                </div>
            )}
        </header>
    );
};