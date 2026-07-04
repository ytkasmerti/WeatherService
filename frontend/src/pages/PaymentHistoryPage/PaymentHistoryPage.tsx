import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {Pagination} from '../../components/ui/Pagination/Pagination.tsx';
import {Button} from '../../components/ui/Button/Button.tsx';
import {paymentApi} from '../../api/authService.ts';
import {useNotification} from '../../hooks/useNotification.ts';
import {useApiAction} from '../../hooks/useApiAction.ts';
import styles from './Styles.module.css';
import '../../App.css';

export const PaymentHistoryPage = () => {
    const [history, setHistory] = useState<any>(null);
    const [page, setPage] = useState(0);
    const {notify, setNotify, clearNotify} = useNotification();
    const {execute, isLoading} = useApiAction();
    const navigate = useNavigate();

    const loadHistory = async (pageIdx: number) => {
        const data = await execute(async () => {
            return await paymentApi.getHistory(pageIdx, 10);
        });

        if (data) {
            setHistory(data);
        } else {
            setNotify("Ошибка загрузки истории");
        }
    };

    useEffect(() => {
        loadHistory(page);
    }, [page]);

    return (
        <div className={styles.wrapper}>
            {notify && <Notification message={notify} onClose={clearNotify}/>}

            <Button text="← Назад в профиль" onClick={() => navigate('/profile')}/>
            <h1>История платежей</h1>

            {history ? (
                <>
                    <table className={styles.table}>
                        <thead>
                        <tr className={styles.headerRow}>
                            <th>Дата</th>
                            <th>Сумма</th>
                            <th>Статус</th>
                        </tr>
                        </thead>
                        <tbody>
                        {history.payments.map((p: any) => (
                            <tr key={p.id} className={styles.dataRow}>
                                <td>{new Date(p.createdAt).toLocaleString()}</td>
                                <td>{p.amount}</td>
                                <td>{p.status}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <Pagination
                        currentPage={page}
                        totalPages={history.totalPages}
                        onPageChange={(p) => setPage(p)}
                    />
                </>
            ) : (
                isLoading ? <p>Загрузка данных...</p> : null
            )}
        </div>
    );
};