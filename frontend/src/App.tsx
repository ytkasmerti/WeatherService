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
import {ProtectedRoute} from "./components/ProtectedRoute/ProtectedRoute";

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right"/>
            <Routes>
                <Route element={<MainLayout/>}>
                    <Route path="/" element={<HomePage/>}/>
                    <Route path="/weather" element={<ProtectedRoute><WeatherPage/></ProtectedRoute>}/>
                    <Route path="/profile" element={<ProtectedRoute><ProfilePage/></ProtectedRoute>}/>
                    <Route path="/subscriptions" element={<ProtectedRoute><SubscriptionPage/></ProtectedRoute>}/>
                </Route>
                <Route element={<MinimalLayout hideHeader={false}/>}>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route path="/forgot-password" element={<ForgotPasswordPage/>}/>
                </Route>
                <Route element={<MinimalLayout hideHeader={true}/>}>
                    <Route path="/payment-history" element={<ProtectedRoute><PaymentHistoryPage/></ProtectedRoute>}/>
                    <Route path="/upgrade" element={<ProtectedRoute><UpgradePage/></ProtectedRoute>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}