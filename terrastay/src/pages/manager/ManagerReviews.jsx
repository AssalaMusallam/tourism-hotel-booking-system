import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { formatDate, listFromResponse, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerReviews = () => {
  const { hotelId, isLoadingHotelId } = useManagerHotel();
  const [reply, setReply] = useState('');

  const { data = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['manager-hotel-reviews', hotelId],
    queryFn: async () => {
      try {
        const response = await api.get('/api/reviews', { params: { hotelId } });
        return listFromResponse(response.data);
      } catch {
        const fallback = await api.get(`/api/hotels/${hotelId}/reviews`);
        return listFromResponse(fallback.data);
      }
    },
    enabled: !!hotelId,
    staleTime: 60000,
  });

  const mutation = useMutation({
    mutationFn: ({ id, action }) => api.post(`/api/reviews/${id}/${action}`, action === 'reply' ? { message: reply } : {}),
    onSuccess: () => refetch(),
    onError: (err) => toast.error(permissionMessage(err)),
  });

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (isLoading) return <div className={styles.loading}>Loading...</div>;
  if (isError) return <div className={styles.empty}>{permissionMessage(error)}</div>;

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>Reviews</h1><p>Reply to or flag reviews for your hotel.</p></div></header>
      <div className={styles.toolbar}><input value={reply} onChange={(event) => setReply(event.target.value)} placeholder="Reply message" /></div>
      <div className={styles.panel}>
        {data.length === 0 ? <div className={styles.empty}>No data found</div> : (
          <table className={styles.table}>
            <thead><tr><th>Guest</th><th>Rating</th><th>Comment</th><th>Date</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>{data.map((row) => (
              <tr key={row.id}>
                <td>{row.guestName || row.guest?.fullName || '-'}</td>
                <td>{'★'.repeat(Number(row.rating || 0))}</td>
                <td>{row.comment}</td>
                <td>{formatDate(row.date || row.createdAt)}</td>
                <td><span className={`${styles.badge} ${styles[String(row.status || 'approved').toLowerCase()]}`}>{row.status || 'approved'}</span></td>
                <td className={styles.actions}>
                  <button className={styles.button} onClick={() => mutation.mutate({ id: row.id, action: 'reply' })}>Reply</button>
                  <button className={styles.danger} onClick={() => mutation.mutate({ id: row.id, action: 'flag' })}>Flag</button>
                </td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
    </section>
  );
};

export default ManagerReviews;
