import { Bell } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/axios';
import { useManagerHotel } from '../../hooks/useManagerHotel';
import { formatDate, listFromResponse, permissionMessage } from './managerUtils';
import styles from './ManagerPages.module.css';

const ManagerNotifications = () => {
  const { hotelId, isLoadingHotelId } = useManagerHotel();
  const { data = [], isLoading, isError, error } = useQuery({
    queryKey: ['manager-notifications', hotelId],
    queryFn: () => api.get('/api/manager/notifications', { params: { hotelId } }).then((response) => listFromResponse(response.data)),
    enabled: !!hotelId,
    staleTime: 60000,
  });

  if (isLoadingHotelId) return <div className={styles.loading}>Loading...</div>;
  if (!hotelId) return <div className={styles.loading}>Connecting to hotel...</div>;
  if (isLoading) return <div className={styles.loading}>Loading...</div>;
  if (isError) return <div className={styles.empty}>{permissionMessage(error)}</div>;

  return (
    <section className={styles.page}>
      <header className={styles.header}><div><h1>Notifications</h1><p>Hotel-scoped manager notifications.</p></div></header>
      <div className={styles.panel}>
        {data.length === 0 ? <div className={styles.empty}>No data found</div> : data.map((item) => (
          <article className={styles.card} key={item.id}>
            <Bell size={18} />
            <strong>{item.title}</strong>
            <p>{item.message}</p>
            <small>{formatDate(item.time || item.createdAt)}</small>
          </article>
        ))}
      </div>
    </section>
  );
};

export default ManagerNotifications;
