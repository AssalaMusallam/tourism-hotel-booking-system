import { useMemo, useState } from 'react';
import { Bell, CheckCircle2, Info } from 'lucide-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../../api/axios';
import { formatDate, normalizePage } from './adminPageUtils';
import styles from './AdminDataPages.module.css';

const MOCK_NOTIFICATIONS = [
  { id: 1, title: 'Booking confirmed', message: 'A new booking was confirmed for Jerusalem Grand Hotel.', time: '2026-05-06T10:20:00', read: false },
  { id: 2, title: 'Payment received', message: 'Payment PAY-1001 was completed successfully.', time: '2026-05-06T11:05:00', read: true },
  { id: 3, title: 'Waiting list update', message: 'A guest joined the waiting list for a suite in Jericho.', time: '2026-05-06T12:30:00', read: false },
];

const fetchNotifications = async ({ filter }) => {
  try {
    const response = await api.get('/api/admin/notifications', { params: { read: filter === 'all' ? undefined : filter === 'read' } });
    return { ...normalizePage(response.data), isMock: false };
  } catch {
    // TODO: replace with real API endpoint when backend admin notifications are available.
    return { ...normalizePage(MOCK_NOTIFICATIONS), isMock: true };
  }
};

const AdminNotifications = () => {
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState('all');
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'notifications-simple', filter],
    queryFn: () => fetchNotifications({ filter }),
    staleTime: 60000,
  });

  const markReadMutation = useMutation({
    mutationFn: () => api.patch('/api/admin/notifications/mark-all-read'),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['admin', 'notifications-simple'] }),
  });

  const rows = useMemo(() => {
    const items = data?.items || [];
    if (filter === 'read') return items.filter((item) => item.read || item.status === 'READ');
    if (filter === 'unread') return items.filter((item) => !(item.read || item.status === 'READ'));
    return items;
  }, [data, filter]);

  return (
    <section className={styles.page}>
      <header className={styles.header}>
        <div>
          <h1>Notifications</h1>
          <p>Review system notifications and mark items as read.</p>
        </div>
        {data?.isMock && <span className={styles.mockNotice}>Mock data</span>}
      </header>

      <div className={styles.toolbar}>
        <select value={filter} onChange={(event) => setFilter(event.target.value)}>
          <option value="all">All</option>
          <option value="unread">Unread</option>
          <option value="read">Read</option>
        </select>
        <button className={styles.primaryButton} onClick={() => markReadMutation.mutate()}>
          <CheckCircle2 size={16} /> Mark all as read
        </button>
      </div>

      <div className={styles.panel}>
        <div className={styles.notificationList}>
          {isLoading ? (
            <div className={styles.empty}>Loading notifications...</div>
          ) : rows.length ? rows.map((item) => {
            const isRead = item.read || item.status === 'READ';
            return (
              <article className={styles.notificationItem} key={item.id}>
                <span className={styles.notificationIcon}>{isRead ? <Info size={18} /> : <Bell size={18} />}</span>
                <div>
                  <h3>{item.title || item.subject || 'Notification'}</h3>
                  <p>{item.message || item.body || '-'}</p>
                  <time>{formatDate(item.time || item.createdAt || item.sentAt)}</time>
                </div>
                <span className={`${styles.badge} ${isRead ? styles.read : styles.unread}`}>{isRead ? 'read' : 'unread'}</span>
              </article>
            );
          }) : (
            <div className={styles.empty}>No notifications found.</div>
          )}
        </div>
      </div>
    </section>
  );
};

export default AdminNotifications;
