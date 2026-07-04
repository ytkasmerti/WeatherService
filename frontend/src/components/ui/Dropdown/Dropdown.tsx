import {useDropdown} from '../../../hooks/useDropdown.ts';
import styles from './Styles.module.css';

interface DropdownProps {
    value: string;
    children: React.ReactNode;
}

export const Dropdown = ({value, children}: DropdownProps) => {
    const {isOpen, toggle, arrow} = useDropdown();

    return (
        <div className={styles.container}>
            <button className={styles.button} onClick={toggle} type="button">
                {value}
                <span>{arrow}</span>
            </button>
            {isOpen && <div className={styles.contextmenu}>{children}</div>}
        </div>
    );
};