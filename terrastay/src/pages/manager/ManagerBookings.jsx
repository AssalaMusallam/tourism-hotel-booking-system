import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { formatDate, getManagedHotelId, money, normalizePage, withMock } from './managerUtils';
import styles from './ManagerPages.module.css';

const mockBookings = [{ id: 1, guestName: 'Guest User', roomTypeName: 'Deluxe Room', checkInDate: '2026-05-12', checkOutDate: '2026-05-14', totalPrice: 320, status: 'PENDING' }];

const ManagerBookings = () => {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [status, setStatus] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const query = useQuery({
    queryKey: ['manager', 'bookings', user?.id, status, dateFrom, dateTo],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get('/api/bookings', { params: { hotelId, status: status || undefined, dateFrom: dateFrom || undefined, dateTo: dateTo || undefined } });
        return { ...normalizePage(response.data), hotelId };
      } catch {
        // TODO: replace with real API endpoint.
        return { ...withMock(mockBookings), hotelId };
      }
    },
    staleTime: 60000,
  });
  const mutation = useMutation({ mutationFn: ({ id, next }) => api.patch(`/api/bookings/${id}/status`, { status: next }), onSettled: () => qc.invalidateQueries({ queryKey: ['manager', 'bookings'] }) });

  return <section className={styles.page}>
    <header className={styles.header}><div><h1>Bookings</h1><p>Bookings for your hotel only.</p></div>{query.data?.isMock && <span className={styles.mock}>Mock data</span>}</header>
    <div className={styles.toolbar}><select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">All statuses</option><option>PENDING</option><option>CONFIRMED</option><option>CANCELLED</option></select><input type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} /><input type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} /></div>
    <div className={styles.panel}><table className={styles.table}><thead><tr><th>Guest Name</th><th>Room</th><th>Check-in</th><th>Check-out</th><th>Total Price</th><th>Status</th><th>Actions</th></tr></thead><tbody>{(query.data?.items || []).map((row) => <tr key={row.id}><td>{row.guestName || row.guest?.fullName}</td><td>{row.roomTypeName || row.roomType?.nameEn || row.roomType?.name}</td><td>{formatDate(row.checkInDate || row.checkIn)}</td><td>{formatDate(row.checkOutDate || row.checkOut)}</td><td>{money(row.totalPrice || row.amount)}</td><td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td><td className={styles.actions}>{row.status === 'PENDING' && <><button className={styles.button} onClick={() => mutation.mutate({ id: row.id, next: 'CONFIRMED' })}>Confirm</button><button className={styles.danger} onClick={() => mutation.mutate({ id: row.id, next: 'CANCELLED' })}>Cancel</button></>}</td></tr>)}</tbody></table></div>
  </section>;
};

export default ManagerBookings;
