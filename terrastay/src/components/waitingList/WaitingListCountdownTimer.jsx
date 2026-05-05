import { useEffect, useMemo, useState } from 'react';
import styles from './WaitingListCountdownTimer.module.css';

const expiryMs = 24 * 60 * 60 * 1000;

const getRemainingMs = (notifiedAt) => {
  if (!notifiedAt) return 0;
  return new Date(notifiedAt).getTime() + expiryMs - Date.now();
};

const formatRemaining = (ms) => {
  if (ms <= 0) return 'Expired';
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return `${hours}h ${minutes}m ${seconds}s remaining`;
};

const WaitingListCountdownTimer = ({ notifiedAt, onExpire }) => {
  const [remaining, setRemaining] = useState(() => getRemainingMs(notifiedAt));

  useEffect(() => {
    setRemaining(getRemainingMs(notifiedAt));
    const id = window.setInterval(() => {
      const next = getRemainingMs(notifiedAt);
      setRemaining(next);
      if (next <= 0) onExpire?.();
    }, 1000);
    return () => window.clearInterval(id);
  }, [notifiedAt, onExpire]);

  const urgent = useMemo(() => remaining > 0 && remaining < 60 * 60 * 1000, [remaining]);

  return (
    <span className={`${styles.timer} ${urgent ? styles.urgent : ''} ${remaining <= 0 ? styles.expired : ''}`}>
      {formatRemaining(remaining)}
    </span>
  );
};

export default WaitingListCountdownTimer;
