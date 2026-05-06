import styles from './PaymentStatusBadge.module.css';
import useLanguage from '../../hooks/useLanguage';

const labels = {
  PENDING: 'paymentPending',
  SUCCESS: 'paymentSuccess',
  FAILED: 'paymentFailed',
  REFUNDED: 'paymentRefunded',
};

const PaymentStatusBadge = ({ status }) => {
  const { t } = useLanguage();
  return (
    <span className={[styles.badge, styles[String(status || '').toLowerCase()]].filter(Boolean).join(' ')}>
      {labels[status] ? t(labels[status]) : status || t('noData')}
    </span>
  );
};

export default PaymentStatusBadge;
