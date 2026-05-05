import { formatDate } from '../../utils/formatDate';
import { formatPrice } from '../../utils/formatPrice';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import styles from './BookingRow.module.css';

const BookingRow = ({ booking, onClick, expanded, onConfirm, onComplete, busy }) => {
  const roomName = booking.roomTypeName || booking.roomType || booking.roomName || '-';

  const stopAndRun = (event, handler) => {
    event.stopPropagation();
    handler?.(booking.id);
  };

  return (
    <>
      <tr className={`${styles.row} ${expanded ? styles.active : ''}`} onClick={onClick}>
        <td className={styles.id}>#{booking.id}</td>
        <td>{booking.guestEmail || booking.guestName}</td>
        <td className={styles.hotel}>{booking.hotelName}</td>
        <td>{roomName}</td>
        <td>{formatDate(booking.checkIn, 'MMM d')}</td>
        <td>{formatDate(booking.checkOut, 'MMM d')}</td>
        <td className={styles.price}>{formatPrice(booking.totalPrice)}</td>
        <td><Badge status={booking.status} /></td>
        <td>
          <div className={styles.actions}>
            {booking.status === 'PENDING' && (
              <Button size="sm" variant="secondary" loading={busy} onClick={(e) => stopAndRun(e, onConfirm)}>
                Confirm
              </Button>
            )}
            {booking.status === 'CONFIRMED' && (
              <Button size="sm" variant="primary" loading={busy} onClick={(e) => stopAndRun(e, onComplete)}>
                Complete
              </Button>
            )}
          </div>
        </td>
      </tr>
      {expanded && (
        <tr className={styles.detailRow}>
          <td colSpan={9}>
            <div className={styles.details}>
              {booking.city && <div><span>City:</span> {booking.city}</div>}
              <div><span>Guest:</span> {booking.guestName}</div>
              <div><span>Email:</span> {booking.guestEmail}</div>
              <div><span>Nights:</span> {booking.nights}</div>
              <div><span>Booked:</span> {formatDate(booking.createdAt)}</div>
            </div>
          </td>
        </tr>
      )}
    </>
  );
};

export default BookingRow;
