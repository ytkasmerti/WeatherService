import type { ReactNode } from "react";
import { useAuth } from "../../hooks/useAuth";
import styles from "./styles.module.css";

interface Props {
    children: ReactNode;
}

export const ProtectedRoute = ({ children }: Props) => {
    const { isAuth } = useAuth();

    if (!isAuth) {
        return (
            <div className={styles.container}>
                <p className={styles.text}>
                    Пожалуйста, авторизуйтесь, чтобы получить доступ к этой странице.
                </p>
            </div>
        );
    }

    return <>{children}</>;
};