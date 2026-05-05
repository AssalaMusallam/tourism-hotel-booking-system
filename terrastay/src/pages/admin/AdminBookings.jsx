import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getAllBookings } from '../../api/bookings';
import BookingRow from '../../components/admin/BookingRow';
import Spinner from '../../components/ui/Spinner';
import Select from '../../components/ui/Select';
import styles from './AdminDashboard.module.css';
import bStyles from './AdminBookings.module.css';

const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'CONFIRMED', label: 'Confirmed' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

const AdminBookings = () => {
  const [statusFilter, setStatusFilter] = useState('');
  const [expandedRow, setExpandedRow] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['admin-bookings', statusFilter],
    queryFn: () => getAllBookings({ status: statusFilter || undefined }),
  });

  const bookings = data?.data || [];

  return (
    <div className={styles.page}>
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/admin" className={styles.navLink}>Dashboard</Link>
          <Link to="/admin/hotels" className={styles.navLink}>Manage Hotels</Link>
          <Link to="/admin/bookings" className={`${styles.navLink} ${styles.active}`}>All Bookings</Link>
        </nav>
      </aside>

      <main className={styles.main}>
        <div className={styles.header}>
          <h1>All Bookings</h1>
          <div className={bStyles.filters}>
            <Select
              options={STATUS_OPTIONS}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              aria-label="Filter by status"
            />
          </div>
        </div>

        <div className={bStyles.summary}>
          <span><strong>{bookings.length}</strong> booking{bookings.length !== 1 ? 's' : ''}</span>
          {statusFilter && <span>filtered by: {statusFilter}</span>}
        </div>

        {isLoading ? <Spinner centered /> : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Booking ID</th>
                  <th>Guest</th>
                  <th>Hotel</th>
                  <th>Room</th>
                  <th>Check-in</th>
                  <th>Check-out</th>
                  <th>Total</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <BookingRow
                    key={b.id}
                    booking={b}
                    expanded={expandedRow === b.id}
                    onClick={() => setExpandedRow(expandedRow === b.id ? null : b.id)}
                  />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
};

export default AdminBookings;
