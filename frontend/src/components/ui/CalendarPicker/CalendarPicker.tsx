import styles from './Styles.module.css';
import {formatDate} from '../../../utils/date.ts';
import {useCalendar} from '../../../hooks/useCalendar.ts';
import {useState} from 'react';

interface CalendarProps {
    onSelect?: (date: string) => void;
    onRangeSelect?: (start: string, end: string) => void;
}

export const CalendarPicker = ({onSelect, onRangeSelect}: CalendarProps) => {
    const {currentDate, days, changeMonth} = useCalendar(); // Хук для логики дат
    const [startDate, setStartDate] = useState<Date | null>(null);
    const [endDate, setEndDate] = useState<Date | null>(null);

    const handleDateClick = (day: Date) => {
        if (onSelect && !onRangeSelect) {
            setStartDate(day);
            onSelect(formatDate(day));
            return;
        }

        if (onRangeSelect) {
            if (!startDate || (startDate && endDate)) {
                setStartDate(day);
                setEndDate(null);
            } else if (day < startDate) {
                setStartDate(day);
            } else {
                setEndDate(day);
                onRangeSelect(formatDate(startDate), formatDate(day));
            }
        }
    };

    return (
        <div className={styles.calendar}>
            <div className={styles.header}>
                <button type="button" onClick={() => changeMonth(-1)}>{'<'}</button>
                <span>{currentDate.toLocaleString('ru', {month: 'long', year: 'numeric'})}</span>
                <button type="button" onClick={() => changeMonth(1)}>{'>'}</button>
            </div>
            <div className={styles.grid}>
                {days.map((day) => {
                    const time = day.getTime();
                    const isStart = startDate?.getTime() === time;
                    const isEnd = endDate?.getTime() === time;
                    const inRange = startDate && endDate && day > startDate && day < endDate;

                    return (
                        <div
                            key={time}
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