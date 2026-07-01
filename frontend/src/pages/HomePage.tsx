export const HomePage = () => (
    <div style={{maxWidth: '900px', margin: '0 auto', fontFamily: 'sans-serif', color: '#333'}}>
        <main style={{padding: '40px 20px'}}>
            <h1 style={{textAlign: 'center', fontSize: '36px', marginBottom: '20px'}}>
                Weather Service — ваш интеллектуальный метео-помощник
            </h1>
            <p style={{textAlign: 'center', fontSize: '18px', color: '#666', marginBottom: '40px'}}>
                Доступ к актуальной погоде, точным прогнозам и глубокой исторической аналитике через современный REST
                API.
            </p>

            <section style={{marginBottom: '50px'}}>
                <h2 style={{borderBottom: '2px solid #2f5bcf', paddingBottom: '10px'}}>Тарифные планы</h2>
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                    gap: '20px',
                    marginTop: '20px'
                }}>
                    <div style={{padding: '20px', border: '1px solid #ddd', borderRadius: '12px'}}>
                        <h3>FREE</h3>
                        <p style={{fontSize: '20px', fontWeight: 'bold'}}>0 ₽/мес</p>
                        <ul style={{paddingLeft: '20px', lineHeight: '1.6'}}>
                            <li>10 запросов к API в день</li>
                            <li>Текущая погода по городу и координатам</li>
                            <li>Базовый функционал доступа</li>
                        </ul>
                    </div>

                    <div style={{
                        padding: '20px',
                        border: '2px solid #2f5bcf',
                        borderRadius: '12px',
                        backgroundColor: '#f8fbff'
                    }}>
                        <h3>BASIC</h3>
                        <p style={{fontSize: '20px', fontWeight: 'bold'}}>500 ₽/мес</p>
                        <ul style={{paddingLeft: '20px', lineHeight: '1.6'}}>
                            <li>100 запросов в день</li>
                            <li>Прогноз на 15 дней</li>
                            <li>История за 7 дней</li>
                            <li>1 активная погодная подписка</li>
                        </ul>
                    </div>

                    <div style={{padding: '20px', border: '1px solid #ddd', borderRadius: '12px'}}>
                        <h3>PREMIUM</h3>
                        <p style={{fontSize: '20px', fontWeight: 'bold'}}>1000 ₽/мес</p>
                        <ul style={{paddingLeft: '20px', lineHeight: '1.6'}}>
                            <li>Безлимитные запросы к API</li>
                            <li>Почасовой прогноз и фильтрация условий</li>
                            <li>Глубокая история (до 5 лет)</li>
                            <li>5 активных подписок с уведомлениями</li>
                        </ul>
                    </div>
                </div>
            </section>

            <section>
                <h2 style={{borderBottom: '2px solid #2f5bcf', paddingBottom: '10px'}}>Почему выбирают нас?</h2>
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                    gap: '25px',
                    marginTop: '20px'
                }}>
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