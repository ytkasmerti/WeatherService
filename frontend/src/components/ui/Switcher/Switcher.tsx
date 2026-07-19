import {useState, useEffect} from 'react';
import styles from './styles.module.css';

interface SwitcherProps {
    isOn?: boolean;
    size?: 'small' | 'large';
    form?: 'round';
    onToggle?: (value: boolean) => void;
}

export const Switcher = ({isOn = false, size = 'large', form = 'round', onToggle}: SwitcherProps) => {
    const [active, setActive] = useState(isOn);
    useEffect(() => {
        setActive(isOn);
    }, [isOn]);

    const handleClick = () => {
        const newValue = !active;
        setActive(newValue);
        if (onToggle) onToggle(newValue);
    };

    const className = [styles.switcher, styles[size], styles[form], active ? styles.active : ''].join(' ');
    return (
        <div className={className} onClick={handleClick}>
            <div className={styles.trigger}/>
        </div>
    );
};