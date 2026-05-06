import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/axios';
import { formatDate, formatMoney, getGuestName, normalizePage } from './adminPageUtils';
import styles from './AdminDataPages.module.css';

const MOCK_PAYMENTS = [
  { id: 'PAY-1001', guestName: 'Samar Khalil', amount: 420, method: 'Credit Card', date: '2026-05-02', status: 'completed' },
  { id: 'PAY-1002', guestName: 'Yousef Ahmad', amount: 260, method: 'Apple Pay', date: '2026-05-03', status: 'pending' },
  { id: 'PAY-1003', guestName: 'Rana Darwish', amount: 180, method: 'Pay at Hotel', date: '2026-05-04', status: 'refunded' },
  { id: 'PAY-1004', guestName: 'Karim Mansour', amount: 310, method: 'Mada', date: '2026-05-05', status: 'failed' },
];

const fetchPayments = async ({ page, status, dateFrom, dateTo }) => {
  try {
    const response = await api.get('/api/admin/payments', { params: { page, size: 10, status: status || undefined, dateFrom: dateFrom || undefined, dateTo: dateTo || undefined } });
    return { ...normalizePage(response.data), isMock: false };
  } catch {
    // TODO: replace with real API endpoint when backend admin payments are available.
    return { ...normalizePage(MOCK_PAYMENTS), isMock: true };
  }
};

const AdminPayments = () => {
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'payments', page, status, dateFrom, dateTo],
    queryFn: () => fetchPayments({ page, status, dateFrom, dateTo }),
    staleTime: 60000,
  });

  const rows = data?.items || [];
  const totalRevenue = useMemo(() => rows.reduce((sum, row) => (
    String(row.status || '').toLowerCase() === 'completed' ? sum + Number(row.amount || row.totalAmount || 0) : sum
  ), 0), [rows]);

  return (
    <section className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1>Payments</h1>
          <p>Track transactions, payment methods, and revenue status.</p>
        </div>
        {data?.isMock && <span className={styles.mockNotice}>Mock data</span>}
      </header>

      <div className={styles.summaryGrid}>
        <div className={styles.summaryCard}><span>Total revenue</span><strong>{formatMoney(totalRevenue)}</strong></div>
        <div className={styles.summaryCard}><span>Transactions</span><strong>{data?.totalElements || rows.length}</strong></div>
        <div className={styles.summaryCard}><span>Completed</span><strong>{rows.filter((row) => String(row.status).toLowerCase() === 'completed').length}</strong></div>
      </div>

      <div className={styles.toolbar}>
        <input type="date" value={dateFrom} onChange={(event) => setDateFrom(event.target.value)} />
        <input type="date" value={dateTo} onChange={(event) => setDateTo(event.target.value)} />
        <select value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }}>
          <option value="">All statuses</option>
          <option value="completed">Completed</option>
          <option value="pending">Pending</option>
          <option value="refunded">Refunded</option>
          <option value="failed">Failed</option>
        </select>
      </div>

      <div className={styles.panel}>
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead><tr><th>Payment ID</th><th>Guest</th><th>Amount</th><th>Method</th><th>Date</th><th>Status</th></tr></thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan="6" className={styles.empty}>Loading payments...</td></tr>
              ) : rows.length ? rows.map((row) => {
                const currentStatus = String(row.status || 'pending').toLowerCase();
                return (
                  <tr key={row.id || row.paymentId}>
                    <td>{row.paymentId || row.id}</td>
                    <td>{getGuestName(row)}</td>
                    <td>{formatMoney(row.amount || row.totalAmount)}</td>
                    <td>{row.method || row.paymentMethod || '-'}</td>
                    <td>{formatDate(row.date || row.createdAt || row.paidAt)}</td>
                    <td><span className={`${styles.badge} ${styles[currentStatus] || styles.neutral}`}>{currentStatus}</span></td>
                  </tr>
                );
              }) : (
                <tr><td colSpan="6" className={styles.empty}>No payments found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
        <div className={styles.pagination}>
          <button disabled={page === 0} onClick={() => setPage((value) => Math.max(0, value - 1))}>Prev</button>
          <button disabled>{page + 1}</button>
          <button disabled={page + 1 >= (data?.totalPages || 1)} onClick={() => setPage((value) => value + 1)}>Next</button>
        </div>
      </div>
    </section>
  );
};

export default AdminPayments;
