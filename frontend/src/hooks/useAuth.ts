import {useState, useEffect} from 'react';

export const useAuth = () => {
    const [isAuth, setIsAuth] = useState(!!localStorage.getItem('token'));
    useEffect(() => {
        const update = () => setIsAuth(!!localStorage.getItem('token'));
        window.addEventListener('authChange', update);
        return () => window.removeEventListener('authChange', update);
    }, []);
    return {isAuth};
};