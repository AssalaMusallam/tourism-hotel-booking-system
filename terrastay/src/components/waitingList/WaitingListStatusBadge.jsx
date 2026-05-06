import styles from './WaitingListStatusBadge.module.css';
import useLanguage from '../../hooks/useLanguage';

const labels = {
  WAITING: 'waitingList',
  NOTIFIED: 'spotAvailable',
  EXPIRED: 'notificationExpires',
  CANCELLED: 'cancelled',
};

const WaitingListStatusBadge = ({ status, children }) => {
  const { t } = useLanguage();
  return (
    <span className={`${styles.badge} ${styles[String(status || 'WAITING').toLowerCase()]}`}>
      {children || (labels[status] ? t(labels[status]) : status)}
    </span>
  );
};

export default WaitingListStatusBadge;
