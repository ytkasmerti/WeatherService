export const Footer = () => {
    return (
        <footer style={{
            marginTop: 'auto',
            padding: '20px',
            background: '#333',
            color: '#ccc',
            textAlign: 'center',
            fontSize: '14px',
            borderTop: '1px solid #444'
        }}>
            Для связи с администрацией или технической поддержкой, пожалуйста, используйте почту -
            <a href="mailto:weather.service@yandex.ru" style={{color: '#fff', marginLeft: '5px'}}>
                weather.service@yandex.ru
            </a>
        </footer>
    );
};