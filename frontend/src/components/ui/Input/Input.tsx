import styles from './styles.module.css';
import React from 'react';

interface InputProps {
    placeholder?: string;

    rightIcon?: React.ReactNode;
    value?: string;
    onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
    type?: "text" | "password";
}

export const Input = ({placeholder, rightIcon, value, onChange, type = "text"}: InputProps) => {
    return (
        <div className={styles.container}>
            <div className={styles.content}>
                <input className={styles.inputField}
                       type={type}
                       placeholder={placeholder}
                       value={value}
                       onChange={onChange}
                />
            </div>{rightIcon && <div className={styles.rightIconWrapper}>{rightIcon}</div>}
        </div>
    );
};