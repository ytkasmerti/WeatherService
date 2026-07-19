import styles from './styles.module.css';
import React from 'react';

interface ButtonProps {
    text?: string;
    onClick?: () => void;
    children?: React.ReactNode;
    isLoading?: boolean;
}

export const Button = ({
                           text = "Button",
                           onClick,
                           children,
                           isLoading = false
                       }: ButtonProps) => {
    return (
        <button className={`${styles.button} ${isLoading ? styles.loading : ''}`}
                onClick={onClick}
                disabled={isLoading}
        >
            {isLoading ? (
                <>
                    <span className={styles.loader}/>
                    <span className={styles.text}>Подождите</span>
                </>
            ) : (
                <>
                    {children}
                    {text && <span className={styles.text}>{text}</span>}
                </>
            )}
        </button>
    );
};