import styles from './StatusBadge.module.css';
import useLanguage from '../../hooks/useLanguage';

const labels = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  CANCELLED: 'cancelled',
  COMPLETED: 'completed',
};

const StatusBadge = ({ status }) => {
  const { t } = useLanguage();
  return (
    <span className={[styles.badge, styles[String(status || '').toLowerCase()]].filter(Boolean).join(' ')}>
      {labels[status] ? t(labels[status]) : status || t('noData')}
    </span>
  );
};

export default StatusBadge;
