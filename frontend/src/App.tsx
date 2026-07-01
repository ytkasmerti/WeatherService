import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {Toaster} from 'react-hot-toast';
import {Header} from './components/Header';
import {HomePage} from './pages/HomePage';
import {LoginPage} from './pages/LoginPage';
import {RegisterPage} from './pages/RegisterPage';
import {ForgotPasswordPage} from './pages/ForgotPasswordPage';
import {WeatherPage} from './pages/WeatherPage';
import {ProfilePage} from './pages/ProfilePage';

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right"/>
            <Header/>
            <div style={{marginTop: '70px'}}>
                <Routes>
                    <Route path="/" element={<HomePage/>}/>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route path="/forgot-password" element={<ForgotPasswordPage/>}/>
                    <Route path="/weather" element={<WeatherPage/>}/>
                    <Route path="/profile" element={<ProfilePage/>}/>
                </Routes>
            </div>
        </BrowserRouter>
    );
}