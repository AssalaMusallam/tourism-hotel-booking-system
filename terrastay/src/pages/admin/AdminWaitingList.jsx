import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../../api/axios';
import { formatDate, getGuestName, getHotelName, getRoomName, normalizePage } from './adminPageUtils';
import styles from './AdminDataPages.module.css';

const MOCK_WAITING_LIST = [
  { id: 1, guestName: 'Lina Haddad', hotelName: 'Bethlehem Hotel', roomTypeName: 'Deluxe Room', requestDate: '2026-05-01', status: 'pending' },
  { id: 2, guestName: 'Omar Saleh', hotelName: 'Jericho Desert Resort', roomTypeName: 'Suite', requestDate: '2026-05-03', status: 'approved' },
  { id: 3, guestName: 'Maya Nasser', hotelName: 'Grand Park Hotel Ramallah', roomTypeName: 'Standard Room', requestDate: '2026-05-04', status: 'rejected' },
];

const fetchWaitingList = async ({ page, status, search }) => {
  try {
    const response = await api.get('/api/admin/waiting-list', { params: { page, size: 10, status: status || undefined, search: search || undefined } });
    return { ...normalizePage(response.data), isMock: false };
  } catch {
    // TODO: replace with real API endpoint when backend admin waiting-list is available.
    return { ...normalizePage(MOCK_WAITING_LIST), isMock: true };
  }
};

const AdminWaitingList = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'waiting-list', page, status, search],
    queryFn: () => fetchWaitingList({ page, status, search }),
    staleTime: 60000,
  });

  const actionMutation = useMutation({
    mutationFn: ({ id, action }) => api.patch(`/api/admin/waiting-list/${id}/${action}`),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['admin', 'waiting-list'] }),
  });

  const rows = useMemo(() => {
    const items = data?.items || [];
    return items.filter((row) => {
      const matchesStatus = !status || String(row.status || '').toLowerCase() === status;
      const haystack = `${getGuestName(row)} ${getHotelName(row)} ${getRoomName(row)}`.toLowerCase();
      return !search || haystack.includes(search.toLowerCase());
    });
  }, [data, search, status]);

  return (
    <section className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1>Waiting List</h1>
          <p>Review room availability requests and approve or reject each entry.</p>
        </div>
        {data?.isMock && <span className={styles.mockNotice}>Mock data</span>}
      </header>

      <div className={styles.toolbar}>
        <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search guest, hotel, or room" />
        <select value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }}>
          <option value="">All statuses</option>
          <option value="pending">Pending</option>
          <option value="approved">Approved</option>
          <option value="rejected">Rejected</option>
        </select>
      </div>

      <div className={styles.panel}>
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr><th>Guest Name</th><th>Hotel</th><th>Room Type</th><th>Request Date</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan="6" className={styles.empty}>Loading waiting list...</td></tr>
              ) : rows.length ? rows.map((row) => {
                const currentStatus = String(row.status || 'pending').toLowerCase();
                return (
                  <tr key={row.id}>
                    <td>{getGuestName(row)}</td>
                    <td>{getHotelName(row)}</td>
                    <td>{getRoomName(row)}</td>
                    <td>{formatDate(row.requestDate || row.createdAt)}</td>
                    <td><span className={`${styles.badge} ${styles[currentStatus] || styles.neutral}`}>{currentStatus}</span></td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.primaryButton} onClick={() => actionMutation.mutate({ id: row.id, action: 'approve' })}>Approve</button>
                        <button className={styles.dangerButton} onClick={() => actionMutation.mutate({ id: row.id, action: 'reject' })}>Reject</button>
                      </div>
                    </td>
                  </tr>
                );
              }) : (
                <tr><td colSpan="6" className={styles.empty}>No waiting list entries found.</td></tr>
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

export default AdminWaitingList;
