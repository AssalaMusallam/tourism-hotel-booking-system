import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { formatDate, getManagedHotelId, money, normalizePage, withMock } from './managerUtils';
import styles from './ManagerPages.module.css';

const mockRows = [{ id: 'PAY-1', guestName: 'Guest User', amount: 320, method: 'Credit Card', date: '2026-05-07', status: 'completed' }];

const ManagerPayments = () => {
  const { user } = useAuth();
  const [status, setStatus] = useState('');
  const query = useQuery({
    queryKey: ['manager', 'payments', user?.id, status],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get('/api/manager/payments', { params: { hotelId, status: status || undefined } });
        return { ...normalizePage(response.data), hotelId };
      } catch {
        // TODO: replace with real API endpoint.
        return { ...withMock(mockRows), hotelId };
      }
    },
    staleTime: 60000,
  });
  return <section className={styles.page}>
    <header className={styles.header}><div><h1>Payments</h1><p>Payments for your hotel only.</p></div>{query.data?.isMock && <span className={styles.mock}>Mock data</span>}</header>
    <div className={styles.toolbar}><select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">All statuses</option><option value="completed">Completed</option><option value="pending">Pending</option><option value="refunded">Refunded</option><option value="failed">Failed</option></select></div>
    <div className={styles.panel}><table className={styles.table}><thead><tr><th>Payment ID</th><th>Guest</th><th>Amount</th><th>Method</th><th>Date</th><th>Status</th></tr></thead><tbody>{(query.data?.items || []).map((row) => <tr key={row.id || row.paymentId}><td>{row.paymentId || row.id}</td><td>{row.guestName || row.guest?.fullName}</td><td>{money(row.amount || row.totalAmount)}</td><td>{row.method || row.paymentMethod}</td><td>{formatDate(row.date || row.createdAt)}</td><td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td></tr>)}</tbody></table></div>
  </section>;
};

export default ManagerPayments;
