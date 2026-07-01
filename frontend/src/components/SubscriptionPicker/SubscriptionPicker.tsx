import {useState} from 'react';
import styles from './SubscriptionPicker.module.css';

interface SubscriptionPickerProps {
    value: string;
    onSelect: (level: string) => void;
}

export const SubscriptionPicker = ({value, onSelect}: SubscriptionPickerProps) => {
    const [isOpen, setIsOpen] = useState(false);
    const options = ['BASIC', 'PREMIUM'];

    return (
        <div className={styles.container}>
            <button className={styles.button} onClick={() => setIsOpen(!isOpen)}>
                {value}
                <span>▼</span>
            </button>

            {isOpen && (
                <div className={styles.contextmenu}>
                    {options.map((opt) => (
                        <div
                            key={opt}
                            className={styles.cMenuItem}
                            onClick={() => {
                                onSelect(opt);
                                setIsOpen(false);
                            }}
                        >
                            {opt}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};