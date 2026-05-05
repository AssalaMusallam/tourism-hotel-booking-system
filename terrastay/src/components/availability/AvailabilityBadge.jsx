import styles from './AvailabilityBadge.module.css';

/**
 * Status badge showing real-time availability with urgency cues.
 *
 * ≥5  → green  "✓ Available"
 * 2-4 → amber  "⚠ Only X left!"
 * 1   → red    "🔥 Last room!" + pulse
 * 0   → gray   "✗ Fully Booked"
 */
const AvailabilityBadge = ({ available, remainingUnits }) => {
  if (!available || remainingUnits === 0) {
    return (
      <div className={styles.stack}>
        <span className={`${styles.badge} ${styles.booked}`}>✗ Fully Booked</span>
      </div>
    );
  }

  if (remainingUnits === 1) {
    return (
      <div className={styles.stack}>
        <span className={`${styles.badge} ${styles.last}`}>🔥 Last room!</span>
        <span className={styles.count}>1 unit remaining</span>
      </div>
    );
  }

  if (remainingUnits <= 4) {
    return (
      <div className={styles.stack}>
        <span className={`${styles.badge} ${styles.scarce}`}>⚠ Only {remainingUnits} left!</span>
        <span className={styles.count}>{remainingUnits} units remaining</span>
      </div>
    );
  }

  return (
    <div className={styles.stack}>
      <span className={`${styles.badge} ${styles.plenty}`}>✓ Available</span>
      <span className={styles.count}>{remainingUnits} units remaining</span>
    </div>
  );
};

export default AvailabilityBadge;
