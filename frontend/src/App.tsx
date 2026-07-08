import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { Header } from './components/ui/Header/Header.tsx';
import { Footer } from './components/ui/Footer/Footer.tsx';
import { HomePage } from './pages/HomePage/HomePage.tsx';
import { LoginPage } from './pages/LoginPage/LoginPage.tsx';
import { RegisterPage } from './pages/RegisterPage/RegisterPage.tsx';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage/ForgotPasswordPage.tsx';
import { WeatherPage } from './pages/WeatherPage/WeatherPage.tsx';
import { ProfilePage } from './pages/ProfilePage/ProfilePage.tsx';
import { PaymentHistoryPage } from './pages/PaymentHistoryPage/PaymentHistoryPage.tsx';
import { UpgradePage } from './pages/UpgradePage/UpgradePage.tsx';
import { SubscriptionPage } from './pages/SubscriptionPage/SubscriptionPage.tsx';

function AppContent() {
    const location = useLocation();
    const hideHeaderPaths = ['/payment-history', '/upgrade'];
    const isHeaderHidden = hideHeaderPaths.includes(location.pathname);
    const minimalHeaderPaths = ['/login', '/register'];
    const isHeaderMinimal = minimalHeaderPaths.includes(location.pathname);

    return (
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            {!isHeaderHidden && <Header variant={isHeaderMinimal ? 'minimal' : 'default'} />}

            <main style={{ flex: 1, marginTop: '70px' }}>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                    <Route path="/weather" element={<WeatherPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                    <Route path="/payment-history" element={<PaymentHistoryPage />} />
                    <Route path="/upgrade" element={<UpgradePage />} />
                    <Route path="/subscriptions" element={<SubscriptionPage />} />
                </Routes>
            </main>

            <Footer />
        </div>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right" />
            <AppContent />
        </BrowserRouter>
    );
}