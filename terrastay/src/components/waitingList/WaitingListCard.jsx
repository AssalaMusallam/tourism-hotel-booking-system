import { formatDistanceToNow } from 'date-fns';
import Button from '../ui/Button';
import NotifiedAlert from './NotifiedAlert';
import WaitingListCountdownTimer from './WaitingListCountdownTimer';
import WaitingListStatusBadge from './WaitingListStatusBadge';
import styles from './WaitingListCard.module.css';

const WaitingListCard = ({ entry, onCancel, onBook }) => {
  const canCancel = entry.status === 'WAITING' || entry.status === 'NOTIFIED';

  return (
    <article className={`${styles.card} ${entry.status === 'NOTIFIED' ? styles.notified : ''}`}>
      <div className={styles.top}>
        <div>
          <h2>{entry.hotelName}</h2>
          <p>{entry.roomTypeName}</p>
        </div>
        <WaitingListStatusBadge status={entry.status}>
          {entry.status === 'WAITING' && entry.positionInQueue ? `In Queue - Position #${entry.positionInQueue}` : undefined}
        </WaitingListStatusBadge>
      </div>

      <div className={styles.meta}>
        <span>{entry.checkIn} to {entry.checkOut}</span>
        <span>Created {entry.createdAt ? formatDistanceToNow(new Date(entry.createdAt), { addSuffix: true }) : '-'}</span>
      </div>

      {entry.status === 'NOTIFIED' && (
        <>
          <div className={styles.actNow}>
            Act Now: <WaitingListCountdownTimer notifiedAt={entry.notifiedAt} />
          </div>
          <NotifiedAlert entry={entry} onBook={onBook} onCancel={onCancel} />
        </>
      )}

      {canCancel && (
        <div className={styles.actions}>
          <Button variant="ghost" size="sm" onClick={() => onCancel?.(entry)}>Cancel</Button>
        </div>
      )}
    </article>
  );
};

export default WaitingListCard;
