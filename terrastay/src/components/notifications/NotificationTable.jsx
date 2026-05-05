import { X } from 'lucide-react';
import toast from 'react-hot-toast';
import { canRetryNotification } from '../../api/notifications';
import { useNotification, useRetryNotification } from '../../hooks/useNotifications';
import { parseApiError } from '../../lib/parseApiError';
import Button from '../ui/Button';
import EmptyState from '../ui/EmptyState';
import Pagination from '../ui/Pagination';
import SectionError from '../ui/SectionError';
import Spinner from '../ui/Spinner';
import NotificationStatusBadge from './NotificationStatusBadge';
import styles from './NotificationTable.module.css';

const formatDateTime = (value) => {
  if (!value) return '-';
  return new Date(value).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
};

const formatType = (value) => value ? value.replaceAll('_', ' ') : '-';

const referenceLabel = (notification) => {
  if (!notification.referenceId && !notification.referenceType) return '-';
  return `${notification.referenceType || 'REFERENCE'} #${notification.referenceId || '-'}`;
};

const DetailItem = ({ label, children, mono }) => (
  <div className={styles.detailItem}>
    <span>{label}</span>
    <strong className={mono ? styles.mono : undefined}>{children || '-'}</strong>
  </div>
);

const DetailDrawer = ({ id, onClose, onRetry }) => {
  const { data, isLoading, isError, error, refetch } = useNotification(id);

  return (
    <div className={styles.drawerOverlay} onClick={onClose}>
      <aside className={styles.drawer} onClick={(event) => event.stopPropagation()}>
        <div className={styles.drawerHeader}>
          <div>
            <p className={styles.muted}>Notification detail</p>
            <h2 className={styles.drawerTitle}>#{id}</h2>
          </div>
          <button type="button" className={styles.close} onClick={onClose} aria-label="Close">
            <X size={20} />
          </button>
        </div>

        {isLoading && <Spinner centered />}
        {isError && <SectionError message={parseApiError(error).message} onRetry={refetch} />}
        {data && (
          <>
            <div className={styles.actions}>
              <NotificationStatusBadge status={data.status} />
              {canRetryNotification(data) && (
                <Button size="sm" variant="secondary" onClick={() => onRetry(data)}>
                  Retry
                </Button>
              )}
            </div>
            <div className={styles.detailGrid}>
              <DetailItem label="ID">{data.id}</DetailItem>
              <DetailItem label="Recipient email">{data.recipientEmail}</DetailItem>
              <DetailItem label="Recipient name">{data.recipientName}</DetailItem>
              <DetailItem label="Type">{formatType(data.type)}</DetailItem>
              <DetailItem label="Status"><NotificationStatusBadge status={data.status} /></DetailItem>
              <DetailItem label="Subject">{data.subject}</DetailItem>
              <DetailItem label="Body preview">{data.bodyPreview || data.body || '-'}</DetailItem>
              <DetailItem label="Reference">{referenceLabel(data)}</DetailItem>
              <DetailItem label="Retry count">{data.retryCount ?? 0}</DetailItem>
              <DetailItem label="Error message">{data.errorMessage}</DetailItem>
              <DetailItem label="Created at">{formatDateTime(data.createdAt)}</DetailItem>
              <DetailItem label="Sent at">{formatDateTime(data.sentAt)}</DetailItem>
              <DetailItem label="Next retry at">{formatDateTime(data.nextRetryAt)}</DetailItem>
              <DetailItem label="Transaction/reference" mono>{data.transactionReference}</DetailItem>
            </div>
          </>
        )}
      </aside>
    </div>
  );
};

const NotificationTable = ({
  data,
  isLoading,
  isError,
  error,
  refetch,
  page,
  onPageChange,
  detailId,
  onOpenDetail,
  onCloseDetail,
}) => {
  const retryMutation = useRetryNotification();
  const notifications = data?.content || [];
  const totalElements = data?.totalElements || 0;
  const totalPages = data?.totalPages || 0;
  const size = data?.size || 20;
  const start = totalElements === 0 ? 0 : page * size + 1;
  const end = Math.min(totalElements, page * size + notifications.length);

  const handleRetry = (notification) => {
    retryMutation.mutate(notification.id, {
      onSuccess: () => toast.success('Notification retry scheduled'),
      onError: (err) => toast.error(parseApiError(err).message),
    });
  };

  if (isLoading) {
    return <div className={styles.tableWrap}><div className="skeleton" style={{ height: 420 }} /></div>;
  }

  if (isError) {
    return <SectionError message={parseApiError(error).message} onRetry={refetch} />;
  }

  if (notifications.length === 0) {
    return (
      <div className={styles.empty}>
        <EmptyState title="No notifications found" description="Try changing filters or send a notification." />
      </div>
    );
  }

  return (
    <>
      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Recipient</th>
              <th>Type</th>
              <th>Status</th>
              <th>Subject</th>
              <th>Reference</th>
              <th>Sent At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {notifications.map((notification) => (
              <tr key={notification.id}>
                <td>#{notification.id}</td>
                <td>
                  <div className={styles.recipient}>
                    <strong>{notification.recipientName || '-'}</strong>
                    <span>{notification.recipientEmail}</span>
                  </div>
                </td>
                <td className={styles.type}>{formatType(notification.type)}</td>
                <td><NotificationStatusBadge status={notification.status} /></td>
                <td className={styles.subject} title={notification.subject}>{notification.subject || '-'}</td>
                <td>{referenceLabel(notification)}</td>
                <td>{formatDateTime(notification.sentAt)}</td>
                <td>
                  <div className={styles.actions}>
                    <Button size="sm" variant="ghost" onClick={() => onOpenDetail(notification.id)}>
                      View
                    </Button>
                    {canRetryNotification(notification) && (
                      <Button
                        size="sm"
                        variant="secondary"
                        loading={retryMutation.isPending && retryMutation.variables === notification.id}
                        onClick={() => handleRetry(notification)}
                      >
                        Retry
                      </Button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className={styles.footer}>
        <span>Showing {start}-{end} of {totalElements}</span>
        <Pagination page={page} totalPages={totalPages} onPageChange={onPageChange} />
      </div>

      {detailId && (
        <DetailDrawer id={detailId} onClose={onCloseDetail} onRetry={handleRetry} />
      )}
    </>
  );
};

export default NotificationTable;
