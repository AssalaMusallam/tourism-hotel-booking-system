import { formatDate } from '../../utils/formatDate';
import { formatPrice } from '../../utils/formatPrice';
import Badge from '../ui/Badge';
import styles from './BookingRow.module.css';

const BookingRow = ({ booking, onClick, expanded }) => {
  return (
    <>
      <tr className={`${styles.row} ${expanded ? styles.active : ''}`} onClick={onClick}>
        <td className={styles.id}>#{booking.id}</td>
        <td>{booking.guestName}</td>
        <td className={styles.hotel}>{booking.hotelName}</td>
        <td>{booking.roomType}</td>
        <td>{formatDate(booking.checkIn, 'MMM d')}</td>
        <td>{formatDate(booking.checkOut, 'MMM d')}</td>
        <td className={styles.price}>{formatPrice(booking.totalPrice)}</td>
        <td><Badge status={booking.status} /></td>
      </tr>
      {expanded && (
        <tr className={styles.detailRow}>
          <td colSpan={8}>
            <div className={styles.details}>
              <div><span>City:</span> {booking.city}</div>
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
