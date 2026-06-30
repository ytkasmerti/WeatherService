import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { Header } from './components/Header'; // Твой хедер
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right" />
            <Header /> {/* Хедер будет всегда вверху */}
            <div style={{ marginTop: '70px' }}> {/* Отступ, чтобы контент не заезжал под Header */}
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                </Routes>
            </div>
        </BrowserRouter>
    );
}