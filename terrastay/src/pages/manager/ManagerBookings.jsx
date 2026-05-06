import { useMemo, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { formatDate, listFromResponse, money, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerBookings = () => {
  const { hotelId, isLoadingHotelId } = useManagerHotel();
  const [status, setStatus] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const { data = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['manager-hotel-bookings', hotelId],
    queryFn: () => api.get(`/api/bookings/hotels/${hotelId}`).then((response) => listFromResponse(response.data)),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  const actionMutation = useMutation({
    mutationFn: ({ id, action }) => api.patch(`/api/bookings/${id}/${action}`),
    onSuccess: () => refetch(),
    onError: (err) => toast.error(permissionMessage(err)),
  });

  const rows = useMemo(() => data.filter((booking) => {
    const bookingStatus = String(booking.status || '');
    const checkIn = booking.checkInDate || booking.checkIn;
    const dateOk = (!dateFrom || checkIn >= dateFrom) && (!dateTo || checkIn <= dateTo);
    return (!status || bookingStatus === status) && dateOk;
  }), [data, dateFrom, dateTo, status]);

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (isLoading) return <div className={styles.loading}>Loading...</div>;
  if (isError) return <div className={styles.empty}>{permissionMessage(error)}</div>;

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>Bookings</h1><p>Bookings for your hotel only.</p></div></header>
      <div className={styles.toolbar}>
        <select value={status} onChange={(event) => setStatus(event.target.value)}>
          <option value="">All statuses</option>
          <option value="PENDING">PENDING</option>
          <option value="CONFIRMED">CONFIRMED</option>
          <option value="COMPLETED">COMPLETED</option>
          <option value="CANCELLED">CANCELLED</option>
        </select>
        <input type="date" value={dateFrom} onChange={(event) => setDateFrom(event.target.value)} />
        <input type="date" value={dateTo} onChange={(event) => setDateTo(event.target.value)} />
      </div>
      <div className={styles.panel}>
        {rows.length === 0 ? <div className={styles.empty}>No data found</div> : (
          <table className={styles.table}>
            <thead><tr><th>Guest Name</th><th>Email</th><th>Room</th><th>Check-in</th><th>Check-out</th><th>Total Price</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>{rows.map((row) => {
              const rowStatus = String(row.status || '');
              return (
                <tr key={row.id}>
                  <td>{row.guestName || row.guest?.fullName || row.user?.fullName || '-'}</td>
                  <td>{row.guestEmail || row.guest?.email || row.user?.email || '-'}</td>
                  <td>{row.roomTypeName || row.roomType?.nameEn || row.roomType?.name || '-'}</td>
                  <td>{formatDate(row.checkInDate || row.checkIn)}</td>
                  <td>{formatDate(row.checkOutDate || row.checkOut)}</td>
                  <td>{money(row.totalPrice || row.amount)}</td>
                  <td><span className={`${styles.badge} ${styles[rowStatus.toLowerCase()]}`}>{rowStatus}</span></td>
                  <td className={styles.actions}>
                    {rowStatus === 'PENDING' && (
                      <>
                        <button className={styles.button} onClick={() => actionMutation.mutate({ id: row.id, action: 'confirm' })}>Confirm</button>
                        <button className={styles.danger} onClick={() => actionMutation.mutate({ id: row.id, action: 'cancel' })}>Cancel</button>
                      </>
                    )}
                    {rowStatus === 'CONFIRMED' && (
                      <>
                        <button className={styles.button} onClick={() => actionMutation.mutate({ id: row.id, action: 'complete' })}>Complete</button>
                        <button className={styles.danger} onClick={() => actionMutation.mutate({ id: row.id, action: 'cancel' })}>Cancel</button>
                      </>
                    )}
                  </td>
                </tr>
              );
            })}</tbody>
          </table>
        )}
      </div>
    </section>
  );
};

export default ManagerBookings;
