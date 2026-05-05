import { useEffect, useState } from 'react';
import NotificationFilters from '../../../components/notifications/NotificationFilters';
import NotificationStatsBar from '../../../components/notifications/NotificationStatsBar';
import NotificationTable from '../../../components/notifications/NotificationTable';
import SendNotificationModal from '../../../components/notifications/SendNotificationModal';
import useAuth from '../../../hooks/useAuth';
import { useNotificationStats, useNotifications } from '../../../hooks/useNotifications';
import styles from './NotificationsPage.module.css';

const useDebouncedValue = (value, delay) => {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const id = window.setTimeout(() => setDebounced(value.trim()), delay);
    return () => window.clearTimeout(id);
  }, [value, delay]);

  return debounced;
};

const NotificationsPage = () => {
  const { isAdmin } = useAuth();
  const [page, setPage] = useState(0);
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState('');
  const [type, setType] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [detailId, setDetailId] = useState(null);
  const debouncedEmail = useDebouncedValue(email, 400);
  const statsQuery = useNotificationStats();
  const listQuery = useNotifications({
    page,
    size: 20,
    email: debouncedEmail || undefined,
    status: status || undefined,
    type: type || undefined,
  });

  useEffect(() => {
    setPage(0);
  }, [debouncedEmail, status, type]);

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Admin</p>
          <h1 className={styles.title}>Notification Management</h1>
          <p className={styles.subtitle}>Monitor delivery, retry failures, and send templated messages.</p>
        </div>
        {!isAdmin && (
          <div className={styles.managerNotice}>
            Managers can send templated notifications. Custom messages are admin-only.
          </div>
        )}
      </header>

      <NotificationStatsBar
        data={statsQuery.data}
        isLoading={statsQuery.isLoading}
        isError={statsQuery.isError}
        error={statsQuery.error}
        refetch={statsQuery.refetch}
        dataUpdatedAt={statsQuery.dataUpdatedAt}
      />

      <NotificationFilters
        email={email}
        status={status}
        type={type}
        onEmailChange={setEmail}
        onStatusChange={setStatus}
        onTypeChange={setType}
        onSend={() => setModalOpen(true)}
      />

      <NotificationTable
        data={listQuery.data}
        isLoading={listQuery.isLoading}
        isError={listQuery.isError}
        error={listQuery.error}
        refetch={listQuery.refetch}
        page={page}
        onPageChange={setPage}
        detailId={detailId}
        onOpenDetail={setDetailId}
        onCloseDetail={() => setDetailId(null)}
      />

      <SendNotificationModal isOpen={modalOpen} onClose={() => setModalOpen(false)} />
    </main>
  );
};

export default NotificationsPage;
