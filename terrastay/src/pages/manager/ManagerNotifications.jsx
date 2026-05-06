import { Bell } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import { formatDate, getManagedHotelId, normalizePage, withMock } from './managerUtils';
import styles from './ManagerPages.module.css';

const mockRows = [{ id: 1, title: 'New booking', message: 'A pending booking needs review.', time: '2026-05-07', read: false }];

const ManagerNotifications = () => {
  const { user } = useAuth();
  const query = useQuery({
    queryKey: ['manager', 'notifications', user?.id],
    queryFn: async () => {
      const hotelId = await getManagedHotelId(user);
      try {
        const response = await api.get('/api/manager/notifications', { params: { hotelId } });
        return { ...normalizePage(response.data), hotelId };
      } catch {
        // TODO: replace with real API endpoint.
        return { ...withMock(mockRows), hotelId };
      }
    },
    staleTime: 60000,
  });
  return <section className={styles.page}>
    <header className={styles.header}><div><h1>Notifications</h1><p>Hotel-scoped manager notifications.</p></div>{query.data?.isMock && <span className={styles.mock}>Mock data</span>}</header>
    <div className={styles.panel}>{(query.data?.items || []).map((item) => <article className={styles.card} key={item.id}><Bell size={18} /><strong>{item.title}</strong><p>{item.message}</p><small>{formatDate(item.time || item.createdAt)}</small></article>)}</div>
  </section>;
};

export default ManagerNotifications;
