import {useEffect} from 'react';
import {authApi} from '../api/authService';
import {useAppDispatch, useAppSelector} from './redux';
import {setAuth} from '../store/authSlice';

export const useAuth = () => {
    const dispatch = useAppDispatch();
    const isAuth = useAppSelector((state) => state.auth.isAuth);
    const checkAuthStatus = async () => {
        try {
            await authApi.check();
            dispatch(setAuth(true));
        } catch {
            dispatch(setAuth(false));
        }
    };

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const login = async (email: string, pass: string) => {
        await authApi.login(email, pass);
        dispatch(setAuth(true));
    };

    const logout = async () => {
        await authApi.logout();
        dispatch(setAuth(false));
    };

    return {isAuth, login, logout, checkAuthStatus};
};