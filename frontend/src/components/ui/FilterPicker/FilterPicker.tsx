import {Dropdown} from '../Dropdown/Dropdown';
import {WEATHER_FILTERS} from '../../../constants/weatherFilters.ts';
import styles from './Styles.module.css';

export const FilterPicker = ({onSelect}: { onSelect: (f: string) => void }) => {
    return (
        <Dropdown value="Выберите фильтр">
            {WEATHER_FILTERS.map((opt) => (
                <div key={opt} className={styles.cMenuItem} onClick={() => onSelect(opt)}>
                    {opt.charAt(0).toUpperCase() + opt.slice(1)}
                </div>
            ))}
        </Dropdown>
    );
};