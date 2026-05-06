import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { formatDate, getManagedHotelId, normalizePage, withMock } from './managerUtils';
import styles from './ManagerPages.module.css';

const mockRows = [{ id: 1, guestName: 'Guest User', roomTypeName: 'Suite', requestDate: '2026-05-07', status: 'pending' }];

const ManagerWaitingList = () => {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [status, setStatus] = useState('');
  const query = useQuery({
    queryKey: ['manager', 'waiting-list', user?.id, status],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get('/api/manager/waiting-list', { params: { hotelId, status: status || undefined } });
        return { ...normalizePage(response.data), hotelId };
      } catch {
        // TODO: replace with real API endpoint.
        return { ...withMock(mockRows), hotelId };
      }
    },
    staleTime: 60000,
  });
  const mutation = useMutation({ mutationFn: ({ id, action }) => api.patch(`/api/manager/waiting-list/${id}/${action}`), onSettled: () => qc.invalidateQueries({ queryKey: ['manager', 'waiting-list'] }) });

  return <section className={styles.page}>
    <header className={styles.header}><div><h1>Waiting List</h1><p>Approve or reject requests for your hotel.</p></div>{query.data?.isMock && <span className={styles.mock}>Mock data</span>}</header>
    <div className={styles.toolbar}><select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">All statuses</option><option value="pending">Pending</option><option value="approved">Approved</option><option value="rejected">Rejected</option></select></div>
    <div className={styles.panel}><table className={styles.table}><thead><tr><th>Guest Name</th><th>Room Type</th><th>Request Date</th><th>Status</th><th>Actions</th></tr></thead><tbody>{(query.data?.items || []).map((row) => <tr key={row.id}><td>{row.guestName || row.guest?.fullName}</td><td>{row.roomTypeName || row.roomType?.nameEn || row.roomType?.name}</td><td>{formatDate(row.requestDate || row.createdAt)}</td><td><span className={`${styles.badge} ${styles[String(row.status).toLowerCase()]}`}>{row.status}</span></td><td className={styles.actions}><button className={styles.button} onClick={() => mutation.mutate({ id: row.id, action: 'approve' })}>Approve</button><button className={styles.danger} onClick={() => mutation.mutate({ id: row.id, action: 'reject' })}>Reject</button></td></tr>)}</tbody></table></div>
  </section>;
};

export default ManagerWaitingList;
