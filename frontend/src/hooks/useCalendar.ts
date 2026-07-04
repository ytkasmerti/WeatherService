import {useState, useMemo} from 'react';

export const useCalendar = (initialDate: Date = new Date()) => {
    const [currentDate, setCurrentDate] = useState(initialDate);

    const days = useMemo(() => {
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        return Array.from({length: daysInMonth}, (_, i) => new Date(year, month, i + 1));
    }, [currentDate]);

    const changeMonth = (offset: number) => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + offset, 1));
    };

    return {currentDate, days, changeMonth};
};