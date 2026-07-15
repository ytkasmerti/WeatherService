import {Dropdown} from '../../ui/Dropdown/Dropdown.tsx';
import {FILTER_ICONS, WEATHER_FILTERS} from '../../../constants/weatherFilters.ts';
import styles from './styles.module.css';

interface FilterPickerProps {
    value: string;
    onSelect: (f: string) => void;
}

export const FilterPicker = ({value, onSelect}: FilterPickerProps) => {
    console.log("FILTER_ICONS:", FILTER_ICONS);
    return (
        <Dropdown value={value || "Выберите фильтр"}>
            {WEATHER_FILTERS.map((opt) => (
                <div key={opt}
                     className={styles.cMenuItem}
                     onClick={() => onSelect(opt)}
                >
                    {FILTER_ICONS[opt] && (
                        <img src={FILTER_ICONS[opt]} alt={opt} className={styles.menuIcon} />
                    )}
                    {opt.charAt(0).toUpperCase() + opt.slice(1)}
                </div>
            ))}
        </Dropdown>
    );
};