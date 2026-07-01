import {useState, useEffect} from 'react';
import {Notification} from '../components/Notification/Notification';
import {Pagination} from '../components/Pagination/Pagination';
import {Button} from '../components/Button/Button';
import {useNavigate} from 'react-router-dom';
import {paymentApi} from '../api/authService';
import '../App.css';

export const PaymentHistoryPage = () => {
    const [history, setHistory] = useState<any>(null);
    const [page, setPage] = useState(0);
    const [notify, setNotify] = useState<string | null>(null);
    const navigate = useNavigate();

    const loadHistory = async (pageIdx: number) => {
        try {
            const data = await paymentApi.getHistory(pageIdx, 10);
            setHistory(data);
        } catch (e: any) {
            setNotify(e.message || "Ошибка загрузки истории");
        }
    };

    useEffect(() => {
        loadHistory(page);
    }, [page]);

    return (
        <div style={{padding: '20px', maxWidth: '800px', margin: '0 auto'}}>
            {notify && <Notification message={notify} onClose={() => setNotify(null)}/>}

            <Button text="← Назад в профиль" onClick={() => navigate('/profile')}/>
            <h1>История платежей</h1>

            {history ? (
                <>
                    <table style={{width: '100%', borderCollapse: 'collapse', marginTop: '20px', textAlign: 'left'}}>
                        <thead>
                        <tr style={{borderBottom: '2px solid #ddd'}}>
                            <th style={{padding: '12px'}}>Дата</th>
                            <th style={{padding: '12px'}}>Сумма</th>
                            <th style={{padding: '10px'}}>Статус</th>
                        </tr>
                        </thead>
                        <tbody>
                        {history.payments.map((p: any) => (
                            <tr key={p.id} style={{borderBottom: '1px solid #eee'}}>
                                <td style={{padding: '12px'}}>{new Date(p.createdAt).toLocaleString()}</td>
                                <td style={{padding: '12px'}}>{p.amount}</td>
                                <td style={{padding: '12px'}}>{p.status}</td>
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
            ) : <p>Загрузка данных...</p>}
        </div>
    );
};