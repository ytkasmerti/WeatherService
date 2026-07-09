import {useNavigate} from 'react-router-dom';
import {LoginForm} from '../../components/widgets/LoginForm/LoginForm.tsx';
import {Button} from '../../components/ui/Button/Button.tsx';
import styles from './Styles.module.css';
import '../../App.css';

export const LoginPage = () => {
    const navigate = useNavigate();
    return (
        <div className={styles.wrapper}>
            <div className={styles.headerWrapper}>
                <Button text="← На главную" onClick={() => navigate('/')}/>
            </div>
            <LoginForm
                onLoginSuccess={() => navigate('/weather')}
                onForgotPassword={() => navigate('/forgot-password')}
                onGoToRegister={() => navigate('/register')}
            />
        </div>
    );
};