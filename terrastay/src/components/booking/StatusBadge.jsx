import styles from './StatusBadge.module.css';

const labels = {
  PENDING: 'Pending',
  CONFIRMED: 'Confirmed',
  CANCELLED: 'Cancelled',
  COMPLETED: 'Completed',
};

const StatusBadge = ({ status }) => (
  <span className={[styles.badge, styles[String(status || '').toLowerCase()]].filter(Boolean).join(' ')}>
    {labels[status] || status || 'Unknown'}
  </span>
);

export default StatusBadge;
