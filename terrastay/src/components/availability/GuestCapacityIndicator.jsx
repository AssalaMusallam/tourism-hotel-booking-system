import { User } from 'lucide-react';
import styles from './GuestCapacityIndicator.module.css';

/**
 * Renders a row of person icons showing capacity vs requested guests.
 *
 * filled (dark)   = requestedGuests
 * outline (gray)  = remaining slots
 * total           = capacity
 */
const GuestCapacityIndicator = ({ capacity, requestedGuests }) => {
  const filled = Math.min(requestedGuests ?? 0, capacity);
  const empty = capacity - filled;
  const hasRequest = requestedGuests !== undefined;

  const statusClass = !hasRequest
    ? ''
    : requestedGuests >= capacity
    ? styles.amber
    : styles.green;

  return (
    <div className={styles.wrap}>
      <div className={styles.icons}>
        {Array.from({ length: filled }, (_, i) => (
          <User key={`f${i}`} size={15} className={styles.filled} strokeWidth={2} />
        ))}
        {Array.from({ length: empty }, (_, i) => (
          <User key={`e${i}`} size={15} className={styles.empty} strokeWidth={1.5} />
        ))}
      </div>
      <span className={`${styles.label} ${statusClass}`}>
        {hasRequest
          ? `${requestedGuests} / ${capacity} guests`
          : `Fits up to ${capacity} guests`}
      </span>
    </div>
  );
};

export default GuestCapacityIndicator;
