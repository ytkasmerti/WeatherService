import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {Button} from '../components/Button/Button';
import {Input} from '../components/Input/Input';
import {Notification} from '../components/Notification/Notification';
import {userApi} from '../api/authService';
import {Switcher} from '../components/Switcher/Switcher';

export const ProfilePage = () => {
    const [profile, setProfile] = useState<any>(null);
    const [notify, setNotify] = useState<string | null>(null);

    const [oldPass, setOldPass] = useState('');
    const [newPass, setNewPass] = useState('');
    const [delPass, setDelPass] = useState('');

    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showDel, setShowDel] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        userApi.getProfile().then(setProfile).catch(e => setNotify(e.message));
    }, []);

    const handleAutoRenewal = async (enabled: boolean) => {
        try {
            await userApi.setAutoRenewal(enabled);
            setNotify(`Автопродление ${enabled ? 'включено' : 'выключено'}`);
            const updated = await userApi.getProfile();
            setProfile(updated);
        } catch (e: any) {
            setNotify(e.message);
        }
    };

    const handleChangePassword = async () => {
        try {
            await userApi.changePassword(oldPass, newPass);
            setNotify("Пароль успешно изменен!");
            setOldPass('');
            setNewPass('');
        } catch (e: any) {
            setNotify(e.message);
        }
    };

    const handleDeleteAccount = async () => {
        if (!confirm("Вы уверены? Удаление аккаунта нельзя отменить.")) return;
        try {
            await userApi.deleteAccount(delPass);
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authChange'));
            navigate('/');
        } catch (e: any) {
            setNotify(e.message);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authChange'));
        setNotify("Вы вышли из аккаунта");
        setTimeout(() => navigate('/'), 1000);
    };

    const EyeButton = (show: boolean, onClick: () => void) => (
        <button type="button" onClick={onClick}
                style={{background: 'none', border: 'none', cursor: 'pointer', display: 'flex'}}>
            {show ? <FaEyeSlash/> : <FaEye/>}
        </button>
    );

    if (!profile) return <div>Загрузка...</div>;

    return (
        <div style={{padding: '20px', maxWidth: '500px', margin: '0 auto'}}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            <h1>Личный кабинет</h1>
            <p>Email: {profile.email}</p>
            <p>Уровень подписки: {profile.subscriptionLevel}</p>

            <div style={{
                marginTop: '20px',
                padding: '15px',
                border: '1px solid #eee',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <p>Автопродление подписки:</p>
                <Switcher
                    isOn={profile.autoRenewal}
                    size="large"
                    form="round"
                    onToggle={(value: boolean) => handleAutoRenewal(value)}
                />
            </div>

            <div style={{marginTop: '50px'}}>
                <h3>Смена пароля</h3>
                <Input type={showOld ? "text" : "password"} placeholder="Старый пароль" value={oldPass}
                       onChange={(e: any) => setOldPass(e.target.value)}
                       rightIcon={EyeButton(showOld, () => setShowOld(!showOld))}/>

                <Input type={showNew ? "text" : "password"} placeholder="Новый пароль" value={newPass}
                       onChange={(e: any) => setNewPass(e.target.value)}
                       rightIcon={EyeButton(showNew, () => setShowNew(!showNew))}/>

                <div style={{display: 'flex', justifyContent: 'center', marginTop: '20px'}}>
                    <Button text="Сохранить пароль" onClick={handleChangePassword}/>
                </div>
            </div>

            <div style={{marginTop: '50px'}}>
                <h3>Удаление аккаунта</h3>
                <Input type={showDel ? "text" : "password"} placeholder="Текущий пароль" value={delPass}
                       onChange={(e: any) => setDelPass(e.target.value)}
                       rightIcon={EyeButton(showDel, () => setShowDel(!showDel))}/>

                <div style={{display: 'flex', justifyContent: 'center', marginTop: '20px'}}>
                    <Button text="Удалить аккаунт" onClick={handleDeleteAccount}/>
                </div>
            </div>

            <div style={{marginTop: '60px', display: 'flex', justifyContent: 'center'}}>
                <Button text="Выйти из аккаунта" onClick={handleLogout}/>
            </div>
        </div>
    );
};