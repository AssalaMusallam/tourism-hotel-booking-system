import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../../api/axios';
import { formatDate, getGuestName, getHotelName, normalizePage } from './adminPageUtils';
import styles from './AdminDataPages.module.css';

const MOCK_REVIEWS = [
  { id: 1, guestName: 'Nour Issa', hotelName: 'Novotel Jerusalem', rating: 5, comment: 'Beautiful stay and helpful team.', date: '2026-05-01', status: 'pending' },
  { id: 2, guestName: 'Adam Qassis', hotelName: 'Al-Qasr Hotel Nablus', rating: 4, comment: 'Clean rooms and a central location near the old market.', date: '2026-05-03', status: 'approved' },
  { id: 3, guestName: 'Dalia Zaid', hotelName: 'InterContinental Jericho', rating: 3, comment: 'Good pool but check-in was slow.', date: '2026-05-04', status: 'pending' },
];

const fetchReviews = async ({ page, rating, hotel }) => {
  try {
    const response = await api.get('/api/admin/reviews', { params: { page, size: 10, rating: rating || undefined, hotel: hotel || undefined } });
    return { ...normalizePage(response.data), isMock: false };
  } catch {
    // TODO: replace with real API endpoint when backend admin reviews are available.
    return { ...normalizePage(MOCK_REVIEWS), isMock: true };
  }
};

const AdminReviews = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [rating, setRating] = useState('');
  const [hotel, setHotel] = useState('');
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'reviews', page, rating, hotel],
    queryFn: () => fetchReviews({ page, rating, hotel }),
    staleTime: 60000,
  });

  const actionMutation = useMutation({
    mutationFn: ({ id, action }) => action === 'delete' ? api.delete(`/api/admin/reviews/${id}`) : api.patch(`/api/admin/reviews/${id}/approve`),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['admin', 'reviews'] }),
  });

  const rows = useMemo(() => {
    const items = data?.items || [];
    return items
      .filter((row) => !rating || Number(row.rating) === Number(rating))
      .filter((row) => !hotel || getHotelName(row).toLowerCase().includes(hotel.toLowerCase()));
  }, [data, hotel, rating]);

  return (
    <section className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1>Reviews</h1>
          <p>Moderate guest feedback and keep public reviews tidy.</p>
        </div>
        {data?.isMock && <span className={styles.mockNotice}>Mock data</span>}
      </header>

      <div className={styles.toolbar}>
        <select value={rating} onChange={(event) => { setRating(event.target.value); setPage(0); }}>
          <option value="">All ratings</option>
          {[5, 4, 3, 2, 1].map((value) => <option key={value} value={value}>{value} stars</option>)}
        </select>
        <input value={hotel} onChange={(event) => setHotel(event.target.value)} placeholder="Filter by hotel" />
      </div>

      <div className={styles.panel}>
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead><tr><th>Guest</th><th>Hotel</th><th>Rating</th><th>Comment</th><th>Date</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan="7" className={styles.empty}>Loading reviews...</td></tr>
              ) : rows.length ? rows.map((row) => {
                const currentStatus = String(row.status || 'pending').toLowerCase();
                return (
                  <tr key={row.id}>
                    <td>{getGuestName(row)}</td>
                    <td>{getHotelName(row)}</td>
                    <td><span className={styles.stars}>{'★'.repeat(Number(row.rating || 0))}{'☆'.repeat(5 - Number(row.rating || 0))}</span></td>
                    <td>{String(row.comment || '').slice(0, 80)}{String(row.comment || '').length > 80 ? '...' : ''}</td>
                    <td>{formatDate(row.date || row.createdAt)}</td>
                    <td><span className={`${styles.badge} ${styles[currentStatus] || styles.neutral}`}>{currentStatus}</span></td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.primaryButton} onClick={() => actionMutation.mutate({ id: row.id, action: 'approve' })}>Approve</button>
                        <button className={styles.dangerButton} onClick={() => actionMutation.mutate({ id: row.id, action: 'delete' })}>Delete</button>
                      </div>
                    </td>
                  </tr>
                );
              }) : (
                <tr><td colSpan="7" className={styles.empty}>No reviews found.</td></tr>
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

export default AdminReviews;
