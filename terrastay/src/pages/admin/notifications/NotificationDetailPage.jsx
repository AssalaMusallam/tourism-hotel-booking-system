import { ArrowLeft } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { canRetryNotification } from '../../../api/notifications';
import NotificationStatusBadge from '../../../components/notifications/NotificationStatusBadge';
import Button from '../../../components/ui/Button';
import SectionError from '../../../components/ui/SectionError';
import Spinner from '../../../components/ui/Spinner';
import { useNotification, useRetryNotification } from '../../../hooks/useNotifications';
import { parseApiError } from '../../../lib/parseApiError';
import styles from './NotificationDetailPage.module.css';

const formatDateTime = (value) => {
  if (!value) return '-';
  return new Date(value).toLocaleString('en-US', {
    month: 'long',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
};

const DetailItem = ({ label, children, wide, mono }) => (
  <div className={`${styles.item} ${wide ? styles.wide : ''}`}>
    <span>{label}</span>
    <strong className={mono ? styles.mono : undefined}>{children || '-'}</strong>
  </div>
);

const NotificationDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const query = useNotification(id);
  const retryMutation = useRetryNotification();
  const notification = query.data;

  const handleRetry = () => {
    retryMutation.mutate(notification.id, {
      onSuccess: () => toast.success('Notification retry scheduled'),
      onError: (error) => toast.error(parseApiError(error).message),
    });
  };

  return (
    <main className={styles.page}>
      <div className={styles.topbar}>
        <Button variant="ghost" icon={ArrowLeft} onClick={() => navigate('/admin/notifications')}>
          Back
        </Button>
      </div>

      {query.isLoading && <Spinner centered />}
      {query.isError && <SectionError message={parseApiError(query.error).message} onRetry={query.refetch} />}

      {notification && (
        <section className={styles.card}>
          <div className={styles.header}>
            <div>
              <p className={styles.eyebrow}>Notification #{notification.id}</p>
              <h1 className={styles.title}>{notification.subject || 'Notification detail'}</h1>
            </div>
            {canRetryNotification(notification) && (
              <Button
                variant="secondary"
                loading={retryMutation.isPending}
                onClick={handleRetry}
              >
                Retry
              </Button>
            )}
          </div>

          <div className={styles.grid}>
            <DetailItem label="ID">{notification.id}</DetailItem>
            <DetailItem label="Status"><NotificationStatusBadge status={notification.status} /></DetailItem>
            <DetailItem label="Recipient email">{notification.recipientEmail}</DetailItem>
            <DetailItem label="Recipient name">{notification.recipientName}</DetailItem>
            <DetailItem label="Type">{notification.type?.replaceAll('_', ' ')}</DetailItem>
            <DetailItem label="Subject">{notification.subject}</DetailItem>
            <DetailItem label="Body preview" wide>{notification.bodyPreview || notification.body}</DetailItem>
            <DetailItem label="Reference ID">{notification.referenceId}</DetailItem>
            <DetailItem label="Reference type">{notification.referenceType}</DetailItem>
            <DetailItem label="Retry count">{notification.retryCount ?? 0}</DetailItem>
            <DetailItem label="Error message">{notification.errorMessage}</DetailItem>
            <DetailItem label="Created at">{formatDateTime(notification.createdAt)}</DetailItem>
            <DetailItem label="Sent at">{formatDateTime(notification.sentAt)}</DetailItem>
            <DetailItem label="Next retry at">{formatDateTime(notification.nextRetryAt)}</DetailItem>
            <DetailItem label="Transaction reference" mono>{notification.transactionReference}</DetailItem>
          </div>
        </section>
      )}
    </main>
  );
};

export default NotificationDetailPage;
