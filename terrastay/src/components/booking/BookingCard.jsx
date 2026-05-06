import { CalendarDays, Hotel, Moon, Star, X } from 'lucide-react';
import Button from '../ui/Button';
import StatusBadge from './StatusBadge';
import PriceDisplay from '../PriceDisplay';
import useLanguage from '../../hooks/useLanguage';
import styles from './BookingCard.module.css';

const BookingCard = ({ booking, onCancel, onReview }) => {
  const { t } = useLanguage();
  const canCancel = booking.status === 'PENDING' || booking.status === 'CONFIRMED';
  const canReview = booking.status === 'COMPLETED';

  return (
    <article className={styles.card}>
      <div className={styles.header}>
        <div className={styles.mainInfo}>
          <div className={styles.titleRow}>
            <h3 className={styles.hotelName}>{booking.hotelNameEn || booking.hotelName}</h3>
            <StatusBadge status={booking.status} />
          </div>
          <p className={styles.roomName}>{booking.roomTypeNameEn || booking.roomTypeName}</p>
          <div className={styles.meta}>
            <span><Hotel size={15} /> {t('bookingReference')} #{booking.id}</span>
            <span><CalendarDays size={15} /> {booking.checkIn} - {booking.checkOut}</span>
            <span><Moon size={15} /> {booking.nights} {t('totalNights')}</span>
          </div>
        </div>
        <div className={styles.summary}>
          <span className={styles.price}><PriceDisplay usdAmount={booking.totalPrice} showOriginal size="md" /></span>
          {canCancel && (
            <Button variant="danger" size="sm" onClick={() => onCancel(booking)}>
              <X size={14} /> {t('cancelBooking')}
            </Button>
          )}
          {canReview && (
            <Button variant="secondary" size="sm" onClick={() => onReview(booking)}>
              <Star size={14} /> {t('writeReview')}
            </Button>
          )}
        </div>
      </div>
    </article>
  );
};

export default BookingCard;
