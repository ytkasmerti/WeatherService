import {useState} from 'react';

export const useApiAction = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const execute = async <T>(action: () => Promise<T>, successMsg?: string): Promise<T | null> => {
        setIsLoading(true);
        setError(null);
        try {
            const result = await action();
            if (successMsg) setError(successMsg);
            return result;
        } catch (e: any) {
            setError(e.message || "Произошла ошибка");
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    return {execute, isLoading, error, setError};
};