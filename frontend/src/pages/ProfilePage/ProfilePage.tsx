import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {useForm, Controller} from 'react-hook-form';
import {FaEye, FaEyeSlash} from "react-icons/fa";
import {Button} from '../../components/ui/Button/Button.tsx';
import {Input} from '../../components/ui/Input/Input.tsx';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {authApi, userApi} from '../../api/authService.ts';
import {Switcher} from '../../components/ui/Switcher/Switcher.tsx';
import {DeleteModal} from '../../components/ui/DeleteModal/DeleteModal.tsx';
import {usePasswordVisibility} from '../../hooks/usePasswordVisibility.ts';
import {useNotification} from '../../hooks/useNotification.ts';
import {profileSchema} from './schema.ts';
import styles from './Styles.module.css';
import '../../App.css';

export const ProfilePage = () => {
    const [profile, setProfile] = useState<any>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isChangeLoading, setIsChangeLoading] = useState(false);
    const [isDeleteLoading, setIsDeleteLoading] = useState(false);
    const {control, handleSubmit, reset} = useForm({defaultValues: {oldPass: '', newPass: '', delPass: ''}});
    const passOld = usePasswordVisibility();
    const passNew = usePasswordVisibility();
    const passDel = usePasswordVisibility();
    const {notify, setNotify, clearNotify} = useNotification();
    const navigate = useNavigate();

    useEffect(() => {
        userApi.getProfile()
            .then(setProfile)
            .catch(e => {
                setNotify(e.message);
                setProfile({error: true});
            });
    }, []);

    if (!profile) {
        return <div className={styles.loading}>Загрузка...</div>;
    }

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

    const handleChangePassword = async (data: any) => {
        try {
            await profileSchema.validateAt('oldPass', {oldPass: data.oldPass});
            await profileSchema.validateAt('newPass', {newPass: data.newPass});
            setIsChangeLoading(true);
            await userApi.changePassword(data.oldPass, data.newPass);
            setNotify("Пароль успешно изменен!");
            reset({oldPass: '', newPass: ''});
        } catch (e: any) {
            setNotify(e.message || "Ошибка смены пароля");
        } finally {
            setIsChangeLoading(false);
        }
    };

    const handleDeleteAccount = async (data: any) => {
        try {
            await profileSchema.validateAt('delPass', {delPass: data.delPass});
            setIsDeleteLoading(true);
            await userApi.deleteAccount(data.delPass);
            setNotify("Ваш аккаунт был успешно удален");
            await authApi.logout();
            window.dispatchEvent(new Event('authChange'));
            setTimeout(() => navigate('/'), 1500);
        } catch (e: any) {
            setNotify(e.message || "Ошибка при удалении аккаунта");
        } finally {
            setIsDeleteLoading(false);
            setIsModalOpen(false);
        }
    };

    const handleLogout = async () => {
        try {
            await authApi.logout();
            window.dispatchEvent(new Event('authChange'));
            navigate('/');
        } catch (e) {
            setNotify("Ошибка при выходе");
        }
    };

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
                <Controller
                    name="oldPass"
                    control={control}
                    render={({field}) => (
                        <Input {...field} type={passOld.show ? "text" : "password"} placeholder="Старый пароль"
                               rightIcon={<button type="button" onClick={passOld.toggle}
                                                  className={styles.iconBtn}>{passOld.show ? <FaEyeSlash/> :
                                   <FaEye/>}</button>}/>
                    )}
                />
                <Controller
                    name="newPass"
                    control={control}
                    render={({field}) => (
                        <Input {...field} type={passNew.show ? "text" : "password"} placeholder="Новый пароль"
                               rightIcon={<button type="button" onClick={passNew.toggle}
                                                  className={styles.iconBtn}>{passNew.show ? <FaEyeSlash/> :
                                   <FaEye/>}</button>}/>
                    )}
                />
                <div className={styles.actionRow}>
                    <Button text="Сохранить пароль" onClick={handleSubmit(handleChangePassword)}
                            isLoading={isChangeLoading}/>
                </div>
            </div>

            <div className={styles.section}>
                <h3>Удаление аккаунта</h3>
                <Controller
                    name="delPass"
                    control={control}
                    render={({field}) => (
                        <Input {...field} type={passDel.show ? "text" : "password"} placeholder="Текущий пароль"
                               rightIcon={<button type="button" onClick={passDel.toggle}
                                                  className={styles.iconBtn}>{passDel.show ? <FaEyeSlash/> :
                                   <FaEye/>}</button>}/>
                    )}
                />
                <div className={styles.actionRow}>
                    <Button text="Удалить аккаунт" onClick={() => setIsModalOpen(true)}/>
                </div>
                {isModalOpen && (
                    <DeleteModal
                        onCancel={() => setIsModalOpen(false)}
                        onConfirm={handleSubmit(handleDeleteAccount)}
                        isLoading={isDeleteLoading}
                    />
                )}
            </div>

            <div className={styles.logoutSection}>
                <Button text="Выйти из аккаунта" onClick={handleLogout}/>
            </div>
        </div>
    );
};