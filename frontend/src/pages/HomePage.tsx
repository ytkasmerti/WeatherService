export const HomePage = () => (
    <div>
        <main style={{ padding: '50px' }}>
            <h1 style={{
                width: '100%',
                margin: '10px 0',
                lineHeight: '1.2',
                fontSize: '40px',
                textAlign: 'center'
            }}>
                Weather Service — прогноз погоды и управление подписками
            </h1>
            <p>Сервис предоставляет REST API для получения текущей погоды, прогнозов и исторических данных.</p>

            <section>
                <h2>Уровни доступа</h2>
                <ul>
                    <li><strong>FREE:</strong> 10 запросов/день, текущая погода.</li>
                    <li><strong>BASIC (500₽):</strong> 100 запросов/день, 1 уведомление, прогноз на 15 дней, история до 7 дней.</li>
                    <li><strong>PREMIUM (1000₽):</strong> Безлимит, 5 уведомлений, почасовой прогноз, фильтрация, история до 5 лет.</li>
                </ul>
            </section>

            <section>
                <h2>Функции</h2>
                <p>Мы используем Redis для кэширования (15 мин) и PostgreSQL для хранения данных. Безопасность обеспечивается Spring Security с использованием JWT/Cookie и BCrypt хеширования.</p>
            </section>
        </main>
    </div>
);