import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { formatDate, getManagedHotelId, money, withMock } from './managerUtils';
import styles from './ManagerPages.module.css';

const mockRows = [{ id: 1, guestName: 'Guest User', room: 'Deluxe Room', checkIn: '2026-05-10', status: 'PENDING' }];

const ManagerDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const query = useQuery({
    queryKey: ['manager', 'dashboard', user?.id, user?.managedHotelId, user?.hotelId],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get('/api/manager/dashboard', { params: { hotelId } });
        return { ...response.data, hotelId, isMock: false };
      } catch {
        // TODO: replace with real API endpoint.
        return { hotelId, hotelName: 'My Hotel', totalBookings: 12, revenue: 8420, occupancyRate: 72, pendingBookings: 3, recentBookings: mockRows, isMock: true };
      }
    },
    staleTime: 60000,
  });
  const data = query.data || {};

  return (
    <section className={styles.page}>
      <header className={styles.header}>
        <div><h1>My Hotel Dashboard</h1><p>{data.hotelName || user?.hotelName || 'Managed hotel'}</p></div>
        {data.isMock && <span className={styles.mock}>Mock data</span>}
      </header>
      <div className={styles.grid}>
        <div className={styles.card}><span>Total bookings this month</span><strong>{data.totalBookings || 0}</strong></div>
        <div className={styles.card}><span>Revenue this month</span><strong>{money(data.revenue)}</strong></div>
        <div className={styles.card}><span>Occupancy rate</span><strong>{data.occupancyRate || 0}%</strong></div>
        <div className={styles.card}><span>Pending bookings</span><strong>{data.pendingBookings || 0}</strong></div>
      </div>
      <div className={styles.actions}>
        <button className={styles.button} onClick={() => navigate('/manager/room-types')}>Add Room Type</button>
        <button className={styles.secondary} onClick={() => navigate('/manager/hotel')}>Edit Hotel Info</button>
        <button className={styles.secondary} onClick={() => navigate('/manager/reviews')}>View Reviews</button>
      </div>
      <div className={styles.panel}>
        <table className={styles.table}>
          <thead><tr><th>Guest</th><th>Room</th><th>Check-in</th><th>Status</th></tr></thead>
          <tbody>{(data.recentBookings || []).map((row) => <tr key={row.id}><td>{row.guestName}</td><td>{row.room}</td><td>{formatDate(row.checkIn)}</td><td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td></tr>)}</tbody>
        </table>
      </div>
    </section>
  );
};

export default ManagerDashboard;
