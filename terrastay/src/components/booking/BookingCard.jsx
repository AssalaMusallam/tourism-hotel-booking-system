import { CalendarDays, Hotel, Moon, Star, X } from 'lucide-react';
import Button from '../ui/Button';
import StatusBadge from './StatusBadge';
import styles from './BookingCard.module.css';

const money = (value) =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const BookingCard = ({ booking, onCancel, onReview }) => {
  const canCancel = booking.status === 'PENDING' || booking.status === 'CONFIRMED';
  const canReview = booking.status === 'COMPLETED';

  return (
    <article className={styles.card}>
      <div className={styles.header}>
        <div className={styles.mainInfo}>
          <div className={styles.titleRow}>
            <h3 className={styles.hotelName}>{booking.hotelName}</h3>
            <StatusBadge status={booking.status} />
          </div>
          <p className={styles.roomName}>{booking.roomTypeName}</p>
          <div className={styles.meta}>
            <span><Hotel size={15} /> Booking #{booking.id}</span>
            <span><CalendarDays size={15} /> {booking.checkIn} to {booking.checkOut}</span>
            <span><Moon size={15} /> {booking.nights} night{booking.nights === 1 ? '' : 's'}</span>
          </div>
        </div>
        <div className={styles.summary}>
          <span className={styles.price}>{money(booking.totalPrice)}</span>
          {canCancel && (
            <Button variant="danger" size="sm" onClick={() => onCancel(booking)}>
              <X size={14} /> Cancel
            </Button>
          )}
          {canReview && (
            <Button variant="secondary" size="sm" onClick={() => onReview(booking)}>
              <Star size={14} /> Write a Review
            </Button>
          )}
        </div>
      </div>
    </article>
  );
};

export default BookingCard;
