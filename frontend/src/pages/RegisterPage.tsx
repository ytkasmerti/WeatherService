import {useNavigate} from 'react-router-dom';
import {RegisterForm} from '../components/RegisterForm/RegisterForm';

export const RegisterPage = () => {
    const navigate = useNavigate();

    return (
        <div style={{display: 'flex', justifyContent: 'center', marginTop: '50px'}}>
            <RegisterForm
                onSuccess={() => navigate('/login')}
                onCancel={() => navigate('/login')}
            />
        </div>
    );
};