import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../../api/axios';
import { formatDate, normalizePage } from './adminPageUtils';
import styles from './AdminDataPages.module.css';

const MOCK_GUESTS = [
  { id: 1, fullName: 'Mariam Odeh', email: 'mariam@example.com', phone: '+970599111222', registrationDate: '2026-04-20', totalBookings: 4, status: 'active' },
  { id: 2, fullName: 'Tariq Sabbagh', email: 'tariq@example.com', phone: '+970599333444', registrationDate: '2026-04-27', totalBookings: 1, status: 'active' },
  { id: 3, fullName: 'Hala Saadeh', email: 'hala@example.com', phone: '+970599555666', registrationDate: '2026-05-02', totalBookings: 0, status: 'blocked' },
];

const fetchGuests = async ({ page, search }) => {
  try {
    const response = await api.get('/api/admin/users', { params: { page, size: 10, role: 'GUEST', search: search || undefined } });
    return { ...normalizePage(response.data), isMock: false };
  } catch {
    // TODO: replace with real API endpoint when backend admin guests endpoint is available.
    return { ...normalizePage(MOCK_GUESTS), isMock: true };
  }
};

const AdminGuests = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'guests', page, search],
    queryFn: () => fetchGuests({ page, search }),
    staleTime: 60000,
  });

  const blockMutation = useMutation({
    mutationFn: (id) => api.patch(`/api/admin/users/${id}/block`),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['admin', 'guests'] }),
  });

  const rows = useMemo(() => {
    const items = data?.items || [];
    return items.filter((row) => {
      const name = row.fullName || row.name || '';
      const email = row.email || '';
      return !search || `${name} ${email}`.toLowerCase().includes(search.toLowerCase());
    });
  }, [data, search]);

  return (
    <section className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1>Guests</h1>
          <p>Search guest accounts, booking history, and account status.</p>
        </div>
        {data?.isMock && <span className={styles.mockNotice}>Mock data</span>}
      </header>

      <div className={styles.toolbar}>
        <input value={search} onChange={(event) => { setSearch(event.target.value); setPage(0); }} placeholder="Search by name or email" />
      </div>

      <div className={styles.panel}>
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Registration Date</th><th>Total Bookings</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan="7" className={styles.empty}>Loading guests...</td></tr>
              ) : rows.length ? rows.map((row) => {
                const currentStatus = String(row.status || (row.active === false ? 'blocked' : 'active')).toLowerCase();
                return (
                  <tr key={row.id}>
                    <td>{row.fullName || row.name || '-'}</td>
                    <td>{row.email || '-'}</td>
                    <td>{row.phone || row.phoneNumber || '-'}</td>
                    <td>{formatDate(row.registrationDate || row.createdAt)}</td>
                    <td>{row.totalBookings ?? row.bookingsCount ?? 0}</td>
                    <td><span className={`${styles.badge} ${currentStatus === 'active' ? styles.completed : styles.failed}`}>{currentStatus}</span></td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.secondaryButton} onClick={() => window.alert(`Guest: ${row.fullName || row.name || row.email}`)}>View</button>
                        <button className={styles.dangerButton} onClick={() => blockMutation.mutate(row.id)}>Block</button>
                      </div>
                    </td>
                  </tr>
                );
              }) : (
                <tr><td colSpan="7" className={styles.empty}>No guests found.</td></tr>
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

export default AdminGuests;
