import {useNavigate} from 'react-router-dom';
import {LoginForm} from '../components/LoginForm/LoginForm';

export const LoginPage = () => {
    const navigate = useNavigate();

    return (
        <div style={{display: 'flex', justifyContent: 'center', marginTop: '50px'}}>
            <LoginForm
                onLoginSuccess={() => navigate('/weather')}
                onForgotPassword={() => navigate('/forgot-password')}
                onGoToRegister={() => navigate('/register')}
            />
        </div>
    );
};