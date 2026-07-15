import styles from './styles.module.css';
import React from 'react';

interface ButtonProps {
    text?: string;
    onClick?: () => void;
    leftIcon?: string;
    rightIcon?: string;
    children?: React.ReactNode;
    isLoading?: boolean;
}

export const Button = ({
                           text = "Button",
                           onClick,
                           children,
                           leftIcon,
                           rightIcon,
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
                    {leftIcon && <img src={leftIcon} className={styles.icon} alt="icon"/>}
                    {children}
                    {text && <span className={styles.text}>{text}</span>}
                    {rightIcon && <img src={rightIcon} className={styles.icon} alt="icon"/>}
                </>
            )}
        </button>
    );
};