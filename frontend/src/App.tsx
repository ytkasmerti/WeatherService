import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {Toaster} from 'react-hot-toast';
import {Header} from './components/Header';
import {Footer} from './components/Footer';
import {HomePage} from './pages/HomePage';
import {LoginPage} from './pages/LoginPage';
import {RegisterPage} from './pages/RegisterPage';
import {ForgotPasswordPage} from './pages/ForgotPasswordPage';
import {WeatherPage} from './pages/WeatherPage';
import {ProfilePage} from './pages/ProfilePage';
import {PaymentHistoryPage} from './pages/PaymentHistoryPage';
import {UpgradePage} from './pages/UpgradePage';
import {SubscriptionPage} from './pages/SubscriptionPage';

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right"/>

            <div style={{
                display: 'flex',
                flexDirection: 'column',
                minHeight: '100vh'
            }}>
                <Header/>
                <main style={{
                    flex: 1,
                    marginTop: '70px'
                }}>
                    <Routes>
                        <Route path="/" element={<HomePage/>}/>
                        <Route path="/login" element={<LoginPage/>}/>
                        <Route path="/register" element={<RegisterPage/>}/>
                        <Route path="/forgot-password" element={<ForgotPasswordPage/>}/>
                        <Route path="/weather" element={<WeatherPage/>}/>
                        <Route path="/profile" element={<ProfilePage/>}/>
                        <Route path="/payment-history" element={<PaymentHistoryPage/>}/>
                        <Route path="/upgrade" element={<UpgradePage/>}/>
                        <Route path="/subscriptions" element={<SubscriptionPage/>}/>
                    </Routes>
                </main>

                <Footer/>
            </div>
        </BrowserRouter>
    );
}