const BASE_URL = 'http://localhost:8080';

export const authApi = {
    // Используем URLSearchParams для передачи данных через ?param=value
    login: async (email: string, password: string) => {
        const params = new URLSearchParams({ email, password });
        const res = await fetch(`${BASE_URL}/auth/login?${params}`, { method: 'POST' });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Ошибка входа");
        return data;
    },
    register: async (email: string, password: string, confirmPassword: string) => {
        // Добавляем confirmPassword, так как твой бэк его требует
        const params = new URLSearchParams({ email, password, confirmPassword });
        const res = await fetch(`${BASE_URL}/auth/register?${params}`, { method: 'POST' });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Ошибка регистрации");
        return data;
    },
    forgotPassword: async (email: string) => {
        const res = await fetch(`${BASE_URL}/auth/forgot-password?email=${email}`, { method: 'POST' });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Ошибка отправки");
        return data;
    },
    resetPassword: async (email: string, code: string, newPassword: string) => {
        const params = new URLSearchParams({ email, code, newPassword });
        const res = await fetch(`${BASE_URL}/auth/reset-password?${params}`, { method: 'POST' });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Ошибка сброса");
        return data;
    }
};