import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { formatDate, listFromResponse, money, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerPayments = () => {
  const { hotelId, isLoadingHotelId } = useManagerHotel();
  const [status, setStatus] = useState('');

  const { data: bookings = [], isLoading, isError, error } = useQuery({
    queryKey: ['manager-bookings-for-payments', hotelId],
    queryFn: () => api.get(`/api/bookings/hotels/${hotelId}`).then((response) => listFromResponse(response.data)),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  const rows = useMemo(() => bookings
    .map((booking) => ({
      id: booking.paymentId || booking.reference || booking.id,
      guestName: booking.guestName || booking.guest?.fullName || booking.guestEmail,
      amount: booking.totalPrice || booking.amount,
      method: booking.paymentMethod || booking.method || 'Booking',
      date: booking.paidAt || booking.createdAt || booking.checkInDate,
      status: booking.paymentStatus || booking.status,
    }))
    .filter((row) => !status || String(row.status).toLowerCase() === status), [bookings, status]);

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (isLoading) return <div className={styles.loading}>Loading...</div>;
  if (isError) return <div className={styles.empty}>{permissionMessage(error)}</div>;

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>Payments</h1><p>Booking-based payment info for your hotel only.</p></div></header>
      <div className={styles.toolbar}>
        <select value={status} onChange={(event) => setStatus(event.target.value)}>
          <option value="">All statuses</option>
          <option value="confirmed">Confirmed</option>
          <option value="completed">Completed</option>
          <option value="pending">Pending</option>
          <option value="cancelled">Cancelled</option>
        </select>
      </div>
      <div className={styles.panel}>
        {rows.length === 0 ? <div className={styles.empty}>No data found</div> : (
          <table className={styles.table}>
            <thead><tr><th>Payment ID</th><th>Guest</th><th>Amount</th><th>Method</th><th>Date</th><th>Status</th></tr></thead>
            <tbody>{rows.map((row) => <tr key={row.id}><td>{row.id}</td><td>{row.guestName || '-'}</td><td>{money(row.amount)}</td><td>{row.method}</td><td>{formatDate(row.date)}</td><td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td></tr>)}</tbody>
          </table>
        )}
      </div>
    </section>
  );
};

export default ManagerPayments;
