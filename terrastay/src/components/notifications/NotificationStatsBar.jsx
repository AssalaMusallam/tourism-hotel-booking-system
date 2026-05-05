import { useEffect, useState } from 'react';
import SectionError from '../ui/SectionError';
import { parseApiError } from '../../lib/parseApiError';
import styles from './NotificationStatsBar.module.css';

const STAT_ITEMS = [
  ['total', 'Total'],
  ['sent', 'Sent'],
  ['failed', 'Failed'],
  ['pending', 'Pending'],
  ['retryScheduled', 'Retry Scheduled'],
  ['permanentlyFailed', 'Permanently Failed'],
  ['sentLast24h', 'Sent Last 24h'],
];

const NotificationStatsBar = ({ data, isLoading, isError, error, refetch, dataUpdatedAt }) => {
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    const id = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, []);

  if (isLoading) {
    return (
      <div className={styles.wrap}>
        <div className={styles.grid}>
          {STAT_ITEMS.map(([key]) => (
            <div key={key} className={`skeleton ${styles.skeleton}`} />
          ))}
        </div>
      </div>
    );
  }

  if (isError) {
    return <SectionError message={parseApiError(error).message} onRetry={refetch} />;
  }

  const secondsAgo = dataUpdatedAt ? Math.max(0, Math.floor((now - dataUpdatedAt) / 1000)) : 0;

  return (
    <section className={styles.wrap}>
      <div className={styles.header}>
        <strong>Delivery overview</strong>
        <span>Last updated {secondsAgo}s ago</span>
      </div>
      <div className={styles.grid}>
        {STAT_ITEMS.map(([key, label]) => (
          <div key={key} className={styles.card}>
            <span className={styles.label}>{label}</span>
            <span className={styles.value}>{Number(data?.[key] || 0).toLocaleString('en-US')}</span>
          </div>
        ))}
      </div>
    </section>
  );
};

export default NotificationStatsBar;
