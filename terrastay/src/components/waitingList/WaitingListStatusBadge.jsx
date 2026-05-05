import styles from './WaitingListStatusBadge.module.css';

const labels = {
  WAITING: 'In Queue',
  NOTIFIED: 'Act Now',
  EXPIRED: 'Expired',
  CANCELLED: 'Cancelled',
};

const WaitingListStatusBadge = ({ status, children }) => (
  <span className={`${styles.badge} ${styles[String(status || 'WAITING').toLowerCase()]}`}>
    {children || labels[status] || status}
  </span>
);

export default WaitingListStatusBadge;
