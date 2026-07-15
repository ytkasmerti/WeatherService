import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {Notification} from '../../components/ui/Notification/Notification.tsx';
import {Pagination} from '../../components/ui/Pagination/Pagination.tsx';
import {Button} from '../../components/ui/Button/Button.tsx';
import {paymentApi} from '../../api/paymentApi.ts';
import {useNotification} from '../../hooks/useNotification.ts';
import styles from './styles.module.css';
import '../../App.css';

export const PaymentHistoryPage = () => {
    const [history, setHistory] = useState<any>(null);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(false);
    const {notify, setNotify, clearNotify} = useNotification();
    const navigate = useNavigate();
    const loadHistory = async (pageIdx: number) => {
        setIsLoading(true);
        try {
            const data = await paymentApi.getHistory(pageIdx, 10);
            setHistory(data);
        } catch (e: any) {
            setNotify(e.message || "Ошибка загрузки истории");
        } finally {
            setIsLoading(false);
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
            {isLoading && !history ? (
                <p>Загрузка данных...</p>
            ) : history ? (
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
            ) : null}
        </div>
    );
};