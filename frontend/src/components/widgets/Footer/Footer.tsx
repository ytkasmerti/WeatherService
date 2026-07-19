import styles from './styles.module.css';

export const Footer = () => {
    return (
        <footer className={styles.footer}>
            Для связи с администрацией или технической поддержкой, пожалуйста, используйте почту -
            <a href="mailto:weather.service@yandex.ru" className={styles.emailLink}>weather.service@yandex.ru</a>
        </footer>
    );
};