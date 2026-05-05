import styles from './PaymentStatusBadge.module.css';

const labels = {
  PENDING: 'Pending',
  SUCCESS: 'Success',
  FAILED: 'Failed',
  REFUNDED: 'Refunded',
};

const PaymentStatusBadge = ({ status }) => (
  <span className={[styles.badge, styles[String(status || '').toLowerCase()]].filter(Boolean).join(' ')}>
    {labels[status] || status || 'Unknown'}
  </span>
);

export default PaymentStatusBadge;
