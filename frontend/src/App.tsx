import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {Toaster} from 'react-hot-toast';
import {MainLayout} from './layouts/MainLayout';
import {MinimalLayout} from './layouts/MinimalLayout';
import {HomePage} from './pages/HomePage/HomePage.tsx';
import {LoginPage} from './pages/LoginPage/LoginPage.tsx';
import {RegisterPage} from './pages/RegisterPage/RegisterPage.tsx';
import {ForgotPasswordPage} from './pages/ForgotPasswordPage/ForgotPasswordPage.tsx';
import {WeatherPage} from './pages/WeatherPage/WeatherPage.tsx';
import {ProfilePage} from './pages/ProfilePage/ProfilePage.tsx';
import {PaymentHistoryPage} from './pages/PaymentHistoryPage/PaymentHistoryPage.tsx';
import {UpgradePage} from './pages/UpgradePage/UpgradePage.tsx';
import {SubscriptionPage} from './pages/SubscriptionPage/SubscriptionPage.tsx';

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right"/>
            <Routes>
                <Route element={<MainLayout/>}>
                    <Route path="/" element={<HomePage/>}/>
                    <Route path="/weather" element={<WeatherPage/>}/>
                    <Route path="/profile" element={<ProfilePage/>}/>
                    <Route path="/subscriptions" element={<SubscriptionPage/>}/>
                </Route>
                <Route element={<MinimalLayout hideHeader={false}/>}>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route path="/forgot-password" element={<ForgotPasswordPage/>}/>
                </Route>
                <Route element={<MinimalLayout hideHeader={true}/>}>
                    <Route path="/payment-history" element={<PaymentHistoryPage/>}/>
                    <Route path="/upgrade" element={<UpgradePage/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}