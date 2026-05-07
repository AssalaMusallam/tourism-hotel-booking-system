import { useMemo, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { formatDate, listFromResponse, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerWaitingList = () => {
  const { hotelId, isLoadingHotelId } = useManagerHotel();
  const [status, setStatus] = useState('');

  const { data = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['manager-waiting-list', hotelId],
    queryFn: () => api.get(`/api/waiting-list/hotel/${hotelId}`).then((response) => listFromResponse(response.data)),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  const mutation = useMutation({
    mutationFn: ({ id, action }) => api.patch(`/api/waiting-list/${id}/${action}`),
    onSuccess: () => refetch(),
    onError: (err) => toast.error(permissionMessage(err)),
  });

  const rows = useMemo(() => data.filter((row) => !status || String(row.status).toLowerCase() === status), [data, status]);

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (isLoading) return <div className={styles.loading}>Loading...</div>;
  if (isError) return <div className={styles.empty}>{permissionMessage(error)}</div>;

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>Waiting List</h1><p>Approve or reject requests for your hotel.</p></div></header>
      <div className={styles.toolbar}>
        <select value={status} onChange={(event) => setStatus(event.target.value)}>
          <option value="">All statuses</option>
          <option value="pending">Pending</option>
          <option value="approved">Approved</option>
          <option value="rejected">Rejected</option>
        </select>
      </div>
      <div className={styles.panel}>
        {rows.length === 0 ? <div className={styles.empty}>No data found</div> : (
          <table className={styles.table}>
            <thead><tr><th>Guest Name</th><th>Room Type</th><th>Request Date</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>{rows.map((row) => (
              <tr key={row.id}>
                <td>{row.guestName || row.guest?.fullName || '-'}</td>
                <td>{row.roomTypeName || row.roomType?.nameEn || row.roomType?.name || '-'}</td>
                <td>{formatDate(row.requestDate || row.createdAt)}</td>
                <td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td>
                <td className={styles.actions}>
                  <button className={styles.button} onClick={() => mutation.mutate({ id: row.id, action: 'approve' })}>Approve</button>
                  <button className={styles.danger} onClick={() => mutation.mutate({ id: row.id, action: 'reject' })}>Reject</button>
                </td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
    </section>
  );
};

export default ManagerWaitingList;
