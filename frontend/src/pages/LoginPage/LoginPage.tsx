import {useNavigate} from 'react-router-dom';
import {LoginForm} from '../../components/ui/LoginForm/LoginForm.tsx';
import styles from './Styles.module.css';
import '../../App.css';

export const LoginPage = () => {
    const navigate = useNavigate();

    return (
        <div className={styles.pageContainer}>
            <LoginForm
                onLoginSuccess={() => navigate('/weather')}
                onForgotPassword={() => navigate('/forgot-password')}
                onGoToRegister={() => navigate('/register')}
            />
        </div>
    );
};