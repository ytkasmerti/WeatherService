import styles from './styles.module.css';
import {useEffect} from "react";

interface NotificationProps {
    message: string;
    onClose: () => void;
}

export const Notification = ({message, onClose}: NotificationProps) => {
    useEffect(() => {
        const timer = setTimeout(onClose, 3000);
        return () => clearTimeout(timer);
    }, [onClose]);
    return (
        <div className={styles.wrapper}>
            <div className={styles.polygon}/>
            <div className={styles.rectangle}>
                <p>{message}</p>
            </div>
        </div>
    );
};