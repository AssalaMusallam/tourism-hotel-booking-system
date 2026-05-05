import styles from './NotificationStatusBadge.module.css';

const LABELS = {
  SENT: 'Sent',
  PENDING: 'Pending',
  FAILED: 'Failed',
  RETRY_SCHEDULED: 'Retry Scheduled',
  PERMANENTLY_FAILED: 'Permanently Failed',
};

const NotificationStatusBadge = ({ status }) => (
  <span className={`${styles.badge} ${styles[status] || styles.PENDING}`}>
    {LABELS[status] || status || 'Unknown'}
  </span>
);

export default NotificationStatusBadge;
