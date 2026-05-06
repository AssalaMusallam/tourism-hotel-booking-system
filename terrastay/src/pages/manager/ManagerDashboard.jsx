import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { formatDate, listFromResponse, money, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerDashboard = () => {
  const navigate = useNavigate();
  const { hotelId, isLoadingHotelId } = useManagerHotel();

  const hotelQuery = useQuery({
    queryKey: ['manager-hotel', hotelId],
    queryFn: () => api.get(`/api/hotels/${hotelId}`).then((response) => response.data),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  const bookingsQuery = useQuery({
    queryKey: ['manager-bookings', hotelId],
    queryFn: () => api.get(`/api/bookings/hotels/${hotelId}`).then((response) => listFromResponse(response.data)),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (hotelQuery.isLoading || bookingsQuery.isLoading) return <div className={styles.loading}>Loading...</div>;

  if (hotelQuery.isError || bookingsQuery.isError) {
    return <div className={styles.empty}>{permissionMessage(hotelQuery.error || bookingsQuery.error)}</div>;
  }

  const bookings = bookingsQuery.data || [];
  const now = new Date();
  const thisMonth = bookings.filter((booking) => {
    const date = new Date(booking.checkInDate || booking.createdAt);
    return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
  });
  const kpis = {
    totalBookings: thisMonth.length,
    pendingBookings: bookings.filter((booking) => booking.status === 'PENDING').length,
    revenue: thisMonth
      .filter((booking) => booking.status === 'CONFIRMED' || booking.status === 'COMPLETED')
      .reduce((sum, booking) => sum + Number(booking.totalPrice || 0), 0),
    occupancyRate: hotelQuery.data?.occupancyRate || 0,
  };
  const recentBookings = bookings.slice(0, 10);

  return (
    <section className={styles.page}>
      <header className={styles.header}>
        <div><h1>My Hotel Dashboard</h1><p>{hotelQuery.data?.nameEn || hotelQuery.data?.name || 'My Hotel'}</p></div>
      </header>
      <div className={styles.grid}>
        <div className={styles.card}><span>Total bookings this month</span><strong>{kpis.totalBookings}</strong></div>
        <div className={styles.card}><span>Revenue this month</span><strong>{money(kpis.revenue)}</strong></div>
        <div className={styles.card}><span>Occupancy rate</span><strong>{kpis.occupancyRate}%</strong></div>
        <div className={styles.card}><span>Pending bookings</span><strong>{kpis.pendingBookings}</strong></div>
      </div>
      <div className={styles.actions}>
        <button className={styles.button} onClick={() => navigate('/manager/room-types')}>Add Room Type</button>
        <button className={styles.secondary} onClick={() => navigate('/manager/hotel')}>Edit Hotel Info</button>
        <button className={styles.secondary} onClick={() => navigate('/manager/reviews')}>View Reviews</button>
      </div>
      <div className={styles.panel}>
        {recentBookings.length === 0 ? <div className={styles.empty}>No data found</div> : (
          <table className={styles.table}>
            <thead><tr><th>Guest</th><th>Room</th><th>Check-in</th><th>Status</th></tr></thead>
            <tbody>{recentBookings.map((row) => <tr key={row.id}><td>{row.guestName || row.guest?.fullName || row.guestEmail}</td><td>{row.roomTypeName || row.roomType?.nameEn || row.roomType?.name}</td><td>{formatDate(row.checkInDate || row.checkIn)}</td><td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td></tr>)}</tbody>
          </table>
        )}
      </div>
    </section>
  );
};

export default ManagerDashboard;
