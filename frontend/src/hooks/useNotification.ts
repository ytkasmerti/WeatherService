import {useState} from 'react';

export const useNotification = (initialState: string | null = null) => {
    const [notify, setNotify] = useState<string | null>(initialState);
    return {notify, setNotify, clearNotify: () => setNotify(null)};
};