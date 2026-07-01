import {useState} from 'react';
import styles from './FilterPicker.module.css';

interface FilterPickerProps {
    onSelect: (filter: string) => void;
}

export const FilterPicker = ({onSelect}: FilterPickerProps) => {
    const [isOpen, setIsOpen] = useState(false);
    const [selected, setSelected] = useState('Выберите фильтр');

    const options = ['дождь', 'солнце', 'облачно', 'снег', 'ветер', 'гроза', 'туман'];

    const handleSelect = (option: string) => {
        setSelected(option);
        onSelect(option);
        setIsOpen(false);
    };

    return (
        <div className={styles.container}>
            <button className={styles.button} onClick={() => setIsOpen(!isOpen)}>
                {selected}
            </button>
            {isOpen && (
                <div className={styles.contextmenu}>
                    {options.map((opt) => (
                        <div key={opt} className={styles.cMenuItem} onClick={() => handleSelect(opt)}>
                            {opt.charAt(0).toUpperCase() + opt.slice(1)}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};