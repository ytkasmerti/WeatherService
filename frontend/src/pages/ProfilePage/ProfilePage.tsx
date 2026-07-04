import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {userApi} from '../../api/authService.ts';
import {Switcher} from '../../components/ui/Switcher/Switcher.tsx';
import {DeleteModal} from '../../components/ui/DeleteModal/DeleteModal.tsx';
import {useApiAction} from '../../hooks/useApiAction.ts';
import {usePasswordVisibility} from '../../hooks/usePasswordVisibility.ts';
import {useNotification} from '../../hooks/useNotification.ts';
import styles from './Styles.module.css';
import '../../App.css';

export const ProfilePage = () => {
    const [profile, setProfile] = useState<any>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const [oldPass, setOldPass] = useState('');
    const [newPass, setNewPass] = useState('');
    const [delPass, setDelPass] = useState('');

    const passOld = usePasswordVisibility();
    const passNew = usePasswordVisibility();
    const passDel = usePasswordVisibility();

    const {notify, setNotify, clearNotify} = useNotification();
    const navigate = useNavigate();

    const changePassAction = useApiAction();
    const deleteAccountAction = useApiAction();

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
        const result = await changePassAction.execute(() => userApi.changePassword(oldPass, newPass), "Пароль успешно изменен!");
        if (result) {
            setOldPass('');
            setNewPass('');
        } else {
            setNotify(changePassAction.error);
        }
    };

    const handleDeleteAccount = async () => {
        const result = await deleteAccountAction.execute(() => userApi.deleteAccount(delPass));
        if (result) {
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('authChange'));
            setTimeout(() => navigate('/'), 1500);
        } else {
            setNotify(deleteAccountAction.error || "Ошибка при удалении");
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        window.dispatchEvent(new Event('authChange'));
        navigate('/');
    };

    if (!profile) {
        return <div className={styles.loading}>Загрузка...</div>;
    }

    return (
        <div className={styles.container}>
            {notify && <Notification message={notify} onClose={clearNotify}/>}

            <h1>Личный кабинет</h1>

            <div className={styles.profileInfo}>
                <p className={styles.textBase}>Email: {profile.email}</p>
                <p className={styles.textBase}>Дата регистрации: {new Date(profile.createdAt).toLocaleDateString()}</p>
                {profile.subscriptionExpiresAt && <p className={styles.textBase}>Подписка
                    истекает: {new Date(profile.subscriptionExpiresAt).toLocaleDateString()}</p>}
                <div className={styles.subscriptionRow}>
                    <p className={styles.textBase}>Уровень подписки: {profile.subscriptionLevel}</p>
                    <Button text="Повысить тариф" onClick={() => navigate('/upgrade')}/>
                </div>
            </div>

            <div className={styles.columnGroup}>
                {profile.limits && (
                    <div className={styles.card}>
                        <p className={styles.limitTitle}>Лимиты запросов:</p>
                        <div className={styles.limitRow}>
                            <span>Всего на день:</span><span>{profile.limits.dailyLimit}</span></div>
                        <div className={styles.limitRow}>
                            <span>Использовано сегодня:</span><span>{profile.limits.usedToday}</span></div>
                        <div className={styles.lastLimitRow}>
                            <span>Осталось:</span><span>{profile.limits.remainingToday}</span></div>
                    </div>
                )}
                <div className={styles.autoRenewalRow}>
                    <p className={styles.textBase}>Автопродление:</p>
                    <Switcher isOn={profile.autoRenewal} size="large" form="round" onToggle={handleAutoRenewal}/>
                </div>
            </div>

            <div className={styles.centerBtn}>
                <Button text="История платежей" onClick={() => navigate('/payment-history')}/>
            </div>

            <div className={styles.section}>
                <h3>Смена пароля</h3>
                <Input type={passOld.show ? "text" : "password"} placeholder="Старый пароль" value={oldPass}
                       onChange={(e: any) => setOldPass(e.target.value)}
                       rightIcon={<button type="button" onClick={passOld.toggle}
                                          className={styles.iconBtn}>{passOld.show ? <FaEyeSlash/> :
                           <FaEye/>}</button>}/>
                <Input type={passNew.show ? "text" : "password"} placeholder="Новый пароль" value={newPass}
                       onChange={(e: any) => setNewPass(e.target.value)}
                       rightIcon={<button type="button" onClick={passNew.toggle}
                                          className={styles.iconBtn}>{passNew.show ? <FaEyeSlash/> :
                           <FaEye/>}</button>}/>
                <div className={styles.actionRow}>
                    <Button text="Сохранить пароль" onClick={handleChangePassword}
                            isLoading={changePassAction.isLoading}/>
                </div>
            </div>

            <div className={styles.section}>
                <h3>Удаление аккаунта</h3>
                <Input type={passDel.show ? "text" : "password"} placeholder="Текущий пароль" value={delPass}
                       onChange={(e: any) => setDelPass(e.target.value)}
                       rightIcon={<button type="button" onClick={passDel.toggle}
                                          className={styles.iconBtn}>{passDel.show ? <FaEyeSlash/> :
                           <FaEye/>}</button>}/>
                <div className={styles.actionRow}>
                    <Button text="Удалить аккаунт" onClick={() => setIsModalOpen(true)}
                            isLoading={deleteAccountAction.isLoading}/>
                </div>
                {isModalOpen && <DeleteModal onCancel={() => setIsModalOpen(false)} onConfirm={handleDeleteAccount}/>}
            </div>

            <div className={styles.logoutSection}>
                <Button text="Выйти из аккаунта" onClick={handleLogout}/>
            </div>
        </div>
    );
};