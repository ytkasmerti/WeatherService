export const HomePage = () => (
    <div style={{maxWidth: '900px', margin: '0 auto', fontFamily: 'sans-serif', color: '#333'}}>
        <main style={{padding: '40px 20px'}}>
            <h1 style={{
                textAlign: 'center', fontSize: '36px', marginBottom: '20px', width: '100%', margin: '0 auto'
            }}>
                Weather Service — ваш персональный метео-помощник
            </h1>
            <p style={{textAlign: 'center', fontSize: '18px', color: '#666', marginBottom: '40px', marginTop: '40px'}}>
                Получайте точные прогнозы, исторические данные и уведомления о погоде в любой точке мира.
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
                        <p><strong>0 ₽/мес</strong></p>
                        <ul style={{paddingLeft: '20px'}}>
                            <li>10 запросов в день</li>
                            <li>Текущая погода</li>
                        </ul>
                    </div>
                    <div style={{
                        padding: '20px',
                        border: '1px solid #2f5bcf',
                        borderRadius: '12px',
                        backgroundColor: '#f0f4f7'
                    }}>
                        <h3>BASIC</h3>
                        <p><strong>500 ₽/мес</strong></p>
                        <ul style={{paddingLeft: '20px'}}>
                            <li>100 запросов в день</li>
                            <li>Прогноз на 15 дней</li>
                            <li>1 погодное уведомление</li>
                        </ul>
                    </div>
                    <div style={{padding: '20px', border: '1px solid #ddd', borderRadius: '12px'}}>
                        <h3>PREMIUM</h3>
                        <p><strong>1000 ₽/мес</strong></p>
                        <ul style={{paddingLeft: '20px'}}>
                            <li>Безлимитные запросы</li>
                            <li>Почасовой прогноз и фильтры</li>
                            <li>История до 5 лет</li>
                            <li>5 погодных уведомлений</li>
                        </ul>
                    </div>
                </div>
            </section>

            <section>
                <h2 style={{borderBottom: '2px solid #2f5bcf', paddingBottom: '10px'}}>Почему выбирают нас?</h2>
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                    gap: '20px',
                    marginTop: '20px'
                }}>
                    <div>
                        <h4>⚡️ Быстро и надежно</h4>
                        <p>Благодаря кэшированию в Redis, вы получаете данные практически мгновенно.</p>
                    </div>
                    <div>
                        <h4>🔔 Умные уведомления</h4>
                        <p>Получайте оповещения об экстремальных погодных явлениях (жара, мороз, ветер).</p>
                    </div>
                    <div>
                        <h4>🔒 Максимальная безопасность</h4>
                        <p>Шифрование данных, двухфакторная аутентификация и надежное хранение паролей.</p>
                    </div>
                    <div>
                        <h4>📊 Глубокая аналитика</h4>
                        <p>Доступ к историческим данным за 5 лет для анализа погоды в прошлом.</p>
                    </div>
                </div>
            </section>
        </main>
    </div>
);