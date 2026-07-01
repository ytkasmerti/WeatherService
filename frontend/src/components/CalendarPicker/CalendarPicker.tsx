import {useState} from 'react';
import styles from './CalendarPicker.module.css';

interface CalendarProps {
    onSelect?: (date: string) => void;
    onRangeSelect?: (start: string, end: string) => void;
}

export const CalendarPicker = ({onRangeSelect}: CalendarProps) => {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [startDate, setStartDate] = useState<Date | null>(null);
    const [endDate, setEndDate] = useState<Date | null>(null);
    const formatDate = (date: Date) => {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    };

    const handleDateClick = (day: Date) => {
        if (!startDate || (startDate && endDate)) {
            setStartDate(day);
            setEndDate(null);
        } else if (day < startDate) {
            setStartDate(day);
        } else {
            setEndDate(day);
            if (onRangeSelect) {
                onRangeSelect(formatDate(startDate), formatDate(day));
            }
        }
    };

    const changeMonth = (offset: number) => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + offset, 1));
    };

    const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate();
    const days = Array.from({length: daysInMonth}, (_, i) => new Date(currentDate.getFullYear(), currentDate.getMonth(), i + 1));

    return (
        <div className={styles.calendar}>
            <div className={styles.header}>
                <button onClick={() => changeMonth(-1)}>{'<'}</button>
                <span>{currentDate.toLocaleString('ru', {month: 'long', year: 'numeric'})}</span>
                <button onClick={() => changeMonth(1)}>{'>'}</button>
            </div>
            <div className={styles.grid}>
                {days.map((day) => {
                    const isStart = startDate && day.getTime() === startDate.getTime();
                    const isEnd = endDate && day.getTime() === endDate.getTime();
                    const inRange = startDate && endDate && day > startDate && day < endDate;

                    return (
                        <div
                            key={day.toString()}
                            className={`${styles.day} ${isStart || isEnd ? styles.selected : ''} ${inRange ? styles.inRange : ''}`}
                            onClick={() => handleDateClick(day)}
                        >
                            {day.getDate()}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};