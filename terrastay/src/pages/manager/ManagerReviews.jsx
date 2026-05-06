import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { formatDate, getManagedHotelId, normalizePage, withMock } from './managerUtils';
import styles from './ManagerPages.module.css';

const mockRows = [{ id: 1, guestName: 'Guest User', rating: 5, comment: 'Excellent stay.', date: '2026-05-07', status: 'approved' }];

const ManagerReviews = () => {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [reply, setReply] = useState('');
  const query = useQuery({
    queryKey: ['manager', 'reviews', user?.id],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get('/api/admin/reviews', { params: { hotelId } });
        return { ...normalizePage(response.data), hotelId };
      } catch {
        // TODO: replace with real API endpoint.
        return { ...withMock(mockRows), hotelId };
      }
    },
    staleTime: 60000,
  });
  const mutation = useMutation({ mutationFn: ({ id, action }) => api.post(`/api/reviews/${id}/${action}`, action === 'reply' ? { message: reply } : {}), onSettled: () => qc.invalidateQueries({ queryKey: ['manager', 'reviews'] }) });

  return <section className={styles.page}>
    <header className={styles.header}><div><h1>Reviews</h1><p>Reply to or flag reviews for your hotel.</p></div>{query.data?.isMock && <span className={styles.mock}>Mock data</span>}</header>
    <div className={styles.toolbar}><input value={reply} onChange={(e) => setReply(e.target.value)} placeholder="Reply message" /></div>
    <div className={styles.panel}><table className={styles.table}><thead><tr><th>Guest</th><th>Rating</th><th>Comment</th><th>Date</th><th>Status</th><th>Actions</th></tr></thead><tbody>{(query.data?.items || []).map((row) => <tr key={row.id}><td>{row.guestName || row.guest?.fullName}</td><td>{'★'.repeat(Number(row.rating || 0))}</td><td>{row.comment}</td><td>{formatDate(row.date || row.createdAt)}</td><td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td><td className={styles.actions}><button className={styles.button} onClick={() => mutation.mutate({ id: row.id, action: 'reply' })}>Reply</button><button className={styles.danger} onClick={() => mutation.mutate({ id: row.id, action: 'flag' })}>Flag</button></td></tr>)}</tbody></table></div>
  </section>;
};

export default ManagerReviews;
