import {Dropdown} from '../Dropdown/Dropdown';
import styles from './Styles.module.css';

export const SubscriptionPicker = ({value, onSelect}: { value: string, onSelect: (l: string) => void }) => {
    const options = ['BASIC', 'PREMIUM'];

    return (
        <Dropdown value={value}>
            {options.map((opt) => (
                <div key={opt} className={styles.cMenuItem} onClick={() => onSelect(opt)}>
                    {opt}
                </div>
            ))}
        </Dropdown>
    );
};