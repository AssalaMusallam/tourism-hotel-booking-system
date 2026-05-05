import { BellRing } from 'lucide-react';
import Button from '../ui/Button';
import WaitingListCountdownTimer from './WaitingListCountdownTimer';
import styles from './NotifiedAlert.module.css';

const NotifiedAlert = ({ entry, onBook, onCancel }) => (
  <div className={styles.alert}>
    <div className={styles.icon}><BellRing size={20} /></div>
    <div className={styles.body}>
      <h3>A room is available for you!</h3>
      <p><strong>{entry.roomTypeName}</strong> at {entry.hotelName}</p>
      <p>Check-in: {entry.checkIn} to Check-out: {entry.checkOut}</p>
      <WaitingListCountdownTimer notifiedAt={entry.notifiedAt} />
      <div className={styles.actions}>
        <Button variant="primary" size="sm" onClick={() => onBook?.(entry)}>Book Now</Button>
        <Button variant="ghost" size="sm" onClick={() => onCancel?.(entry)}>Cancel my spot</Button>
      </div>
    </div>
  </div>
);

export default NotifiedAlert;
