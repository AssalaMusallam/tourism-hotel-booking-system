import styles from './MultiplierBadge.module.css';

const MultiplierBadge = ({ multiplier }) => {
  const m = Number(multiplier ?? 1);
  const pct = Math.round(Math.abs(m - 1) * 100);

  if (m > 1) {
    return <span className={`${styles.badge} ${styles.increase}`}>×{m.toFixed(2)} ▲ +{pct}%</span>;
  }
  if (m < 1) {
    return <span className={`${styles.badge} ${styles.discount}`}>×{m.toFixed(2)} ▼ -{pct}%</span>;
  }
  return <span className={`${styles.badge} ${styles.standard}`}>×1.00 standard</span>;
};

export default MultiplierBadge;
