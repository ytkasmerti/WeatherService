import {useState, useEffect} from 'react';
import {authApi} from '../api/authService';

export const useAuth = () => {
    const [isAuth, setIsAuth] = useState<boolean | null>(null);
    const checkAuthStatus = async () => {
        try {
            await authApi.check();
            setIsAuth(true);
        } catch {
            setIsAuth(false);
        }
    };
    useEffect(() => {
        checkAuthStatus();
        window.addEventListener('authChange', checkAuthStatus);
        return () => window.removeEventListener('authChange', checkAuthStatus);
    }, []);
    return {isAuth};
};