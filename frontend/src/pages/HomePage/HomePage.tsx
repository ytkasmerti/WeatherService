import styles from './styles.module.css';
import '../../App.css';

export const HomePage = () => (
    <div className={styles.wrapper}>
        <main className={styles.main}>
            <h1 className={styles.heroTitle}>
                Weather Service — ваш интеллектуальный метео-помощник
            </h1>
            <p className={styles.heroSubtitle}>
                Доступ к актуальной погоде, точным прогнозам и глубокой исторической аналитике через современный REST
                API.
            </p>

            <section className={styles.section}>
                <h2 className={styles.sectionTitle}>Тарифные планы</h2>
                <div className={styles.grid}>
                    <div className={styles.card}>
                        <h3>FREE</h3>
                        <p className={styles.price}>0 ₽/мес</p>
                        <ul className={styles.list}>
                            <li>10 запросов к API в день</li>
                            <li>Текущая погода по городу и координатам</li>
                            <li>Базовый функционал доступа</li>
                        </ul>
                    </div>

                    <div className={`${styles.card} ${styles.cardHighlighted}`}>
                        <h3>BASIC</h3>
                        <p className={styles.price}>500 ₽/мес</p>
                        <ul className={styles.list}>
                            <li>100 запросов в день</li>
                            <li>Прогноз на 15 дней</li>
                            <li>История за 7 дней</li>
                            <li>1 активная погодная подписка</li>
                        </ul>
                    </div>

                    <div className={styles.card}>
                        <h3>PREMIUM</h3>
                        <p className={styles.price}>1000 ₽/мес</p>
                        <ul className={styles.list}>
                            <li>Безлимитные запросы к API</li>
                            <li>Почасовой прогноз и фильтрация условий</li>
                            <li>Глубокая история (до 5 лет)</li>
                            <li>5 активных подписок с уведомлениями</li>
                        </ul>
                    </div>
                </div>
            </section>

            <section className={styles.section}>
                <h2 className={styles.sectionTitle}>Почему выбирают нас?</h2>
                <div className={styles.grid}>
                    <div>
                        <h4>Погода по всему миру</h4>
                        <p>Получайте данные о состоянии атмосферы в любой точке земного шара — от мегаполисов до самых
                            удаленных уголков.</p>
                    </div>
                    <div>
                        <h4>Интеллектуальные алерты</h4>
                        <p>Система уведомлений о жаре, морозе, сильном ветре или осадках в выбранных вами локациях.
                            Рассылка производится ежедневно в <b>08:00</b> по московскому времени.</p>
                    </div>
                    <div>
                        <h4>Профессиональная аналитика</h4>
                        <p>Широкий спектр исторических данных: от давления и влажности до UV-индекса и времени
                            заката.</p>
                    </div>
                </div>
            </section>
        </main>
    </div>
);