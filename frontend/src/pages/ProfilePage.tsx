import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {Button} from '../components/Button/Button';
import {Input} from '../components/Input/Input';
import {Notification} from '../components/Notification/Notification';
import {userApi} from '../api/authService';
import {Switcher} from '../components/Switcher/Switcher';
import {DeleteModal} from '../components/DeleteModal/DeleteModal';

export const ProfilePage = () => {
    const [profile, setProfile] = useState<any>(null);
    const [notify, setNotify] = useState<string | null>(null);
    const [oldPass, setOldPass] = useState('');
    const [newPass, setNewPass] = useState('');
    const [delPass, setDelPass] = useState('');
    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showDel, setShowDel] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isDeleteLoading, setIsDeleteLoading] = useState(false);
    const navigate = useNavigate();
    const [isModalOpen, setIsModalOpen] = useState(false);

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
        if (!oldPass || !newPass) {
            setNotify("Заполните оба поля пароля");
            return;
        }
        setIsLoading(true);
        try {
            await userApi.changePassword(oldPass, newPass);
            setNotify("Пароль успешно изменен!");
            setOldPass('');
            setNewPass('');
        } catch (e: any) {
            setNotify(e.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteAccount = async () => {
        setIsDeleteLoading(true); // Включает индикатор загрузки
        try {
            await userApi.deleteAccount(delPass);
            setNotify("Аккаунт удален");
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authChange'));
            setTimeout(() => {
                navigate('/');
            }, 1500);
        } catch (e: any) {
            setNotify(e.message || "Ошибка при удалении");
            setIsDeleteLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authChange'));
        setNotify("Вы вышли из аккаунта");
        setTimeout(() => navigate('/'), 1000);
    };

    if (!profile) {
        return <div style={{padding: '20px', textAlign: 'center'}}>Загрузка...</div>;
    }

    return (
        <div style={{padding: '20px', maxWidth: '500px', margin: '0 auto'}}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            <h1>Личный кабинет</h1>

            <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                gap: '10px',
                marginBottom: '20px'
            }}>
                <p style={{margin: 0}}>Email: {profile.email}</p>
                <p style={{margin: 0}}>Дата регистрации: {new Date(profile.createdAt).toLocaleDateString()}</p>
                {profile.subscriptionExpiresAt && (
                    <p style={{margin: 0}}>Подписка
                        истекает: {new Date(profile.subscriptionExpiresAt).toLocaleDateString()}</p>
                )}
                <div style={{display: 'flex', alignItems: 'center', gap: '10px'}}>
                    <p style={{margin: 0}}>Уровень подписки: {profile.subscriptionLevel}</p>
                    <Button text="Повысить тариф" onClick={() => navigate('/upgrade')}/>
                </div>

            </div>
            <div style={{display: 'flex', flexDirection: 'column', gap: '15px', width: '100%'}}>
                {profile.limits && (
                    <div style={{
                        padding: '15px',
                        border: '1px solid #eee',
                        borderRadius: '8px',
                        width: '100%',
                        boxSizing: 'border-box'
                    }}>
                        <p style={{margin: '0 0 10px 0', fontWeight: 'bold'}}>Лимиты запросов:</p>
                        <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '5px'}}>
                            <span>Всего на день:</span> <span>{profile.limits.dailyLimit}</span>
                        </div>
                        <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '5px'}}>
                            <span>Использовано сегодня:</span> <span>{profile.limits.usedToday}</span>
                        </div>
                        <div style={{display: 'flex', justifyContent: 'space-between'}}>
                            <span>Осталось:</span> <span>{profile.limits.remainingToday}</span>
                        </div>
                    </div>
                )}

                <div style={{
                    padding: '15px',
                    border: '1px solid #eee',
                    borderRadius: '8px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    width: '100%',
                    boxSizing: 'border-box'
                }}>
                    <p style={{margin: 0}}>Автопродление:</p>
                    <Switcher isOn={profile.autoRenewal} size="large" form="round" onToggle={handleAutoRenewal}/>
                </div>
            </div>

            <div style={{display: 'flex', justifyContent: 'center', marginTop: '30px'}}>
                <Button text="История платежей" onClick={() => navigate('/payment-history')}/>
            </div>

            <div style={{marginTop: '50px'}}>
                <h3>Смена пароля</h3>
                <Input type={showOld ? "text" : "password"} placeholder="Старый пароль" value={oldPass}
                       onChange={(e: any) => setOldPass(e.target.value)}
                       rightIcon={<button type="button" onClick={() => setShowOld(!showOld)}
                                          style={{background: 'none', border: 'none', cursor: 'pointer'}}>{showOld ?
                           <FaEyeSlash/> : <FaEye/>}</button>}/>
                <Input type={showNew ? "text" : "password"} placeholder="Новый пароль" value={newPass}
                       onChange={(e: any) => setNewPass(e.target.value)}
                       rightIcon={<button type="button" onClick={() => setShowNew(!showNew)}
                                          style={{background: 'none', border: 'none', cursor: 'pointer'}}>{showNew ?
                           <FaEyeSlash/> : <FaEye/>}</button>}/>
                <div style={{display: 'flex', justifyContent: 'center', marginTop: '20px'}}>
                    <Button text="Сохранить пароль" onClick={handleChangePassword} isLoading={isLoading}/>
                </div>
            </div>

            <div style={{marginTop: '50px'}}>
                <h3>Удаление аккаунта</h3>
                <Input type={showDel ? "text" : "password"} placeholder="Текущий пароль" value={delPass}
                       onChange={(e: any) => setDelPass(e.target.value)}
                       rightIcon={<button type="button" onClick={() => setShowDel(!showDel)}
                                          style={{background: 'none', border: 'none', cursor: 'pointer'}}>{showDel ?
                           <FaEyeSlash/> : <FaEye/>}</button>}/>
                <div style={{display: 'flex', justifyContent: 'center', marginTop: '20px'}}>
                    <Button text="Удалить аккаунт" onClick={() => setIsModalOpen(true)} isLoading={isDeleteLoading}/>
                </div>
                {isModalOpen && <DeleteModal onCancel={() => setIsModalOpen(false)} onConfirm={handleDeleteAccount}/>}
            </div>

            <div style={{marginTop: '60px', display: 'flex', justifyContent: 'center'}}>
                <Button text="Выйти из аккаунта" onClick={handleLogout}/>
            </div>
        </div>
    );
};