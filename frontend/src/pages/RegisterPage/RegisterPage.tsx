import {useNavigate} from 'react-router-dom';
import {RegisterForm} from '../../components/widgets/RegisterForm/RegisterForm';
import styles from './styles.module.css';
import '../../App.css';

export const RegisterPage = () => {
    const navigate = useNavigate();

    return (
        <div className={styles.container}>
            <RegisterForm
                onSuccess={() => navigate('/login')}
                onCancel={() => navigate('/login')}
            />
        </div>
    );
};