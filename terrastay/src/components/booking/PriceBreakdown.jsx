import { formatPrice } from '../../utils/formatPrice';
import { formatDate } from '../../utils/formatDate';
import { getNights } from '../../utils/formatDate';
import styles from './PriceBreakdown.module.css';

const PriceBreakdown = ({ hotel, room, checkIn, checkOut }) => {
  const nights = getNights(checkIn, checkOut);
  const roomTotal = (room?.pricePerNight || 0) * nights;
  const taxes = Math.round(roomTotal * 0.12);
  const total = roomTotal + taxes;

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>Price Summary</h3>

      {hotel && (
        <div className={styles.hotelInfo}>
          <strong>{hotel.name}</strong>
          <span>{hotel.city}</span>
        </div>
      )}

      {room && (
        <div className={styles.roomInfo}>
          <span>{room.type}</span>
        </div>
      )}

      {checkIn && checkOut && (
        <div className={styles.dates}>
          <span>{formatDate(checkIn)}</span>
          <span>→</span>
          <span>{formatDate(checkOut)}</span>
        </div>
      )}

      <div className={styles.divider} />

      <div className={styles.lineItems}>
        <div className={styles.lineItem}>
          <span>{formatPrice(room?.pricePerNight || 0)} × {nights} night{nights !== 1 ? 's' : ''}</span>
          <span>{formatPrice(roomTotal)}</span>
        </div>
        <div className={styles.lineItem}>
          <span>Taxes & fees (12%)</span>
          <span>{formatPrice(taxes)}</span>
        </div>
      </div>

      <div className={styles.divider} />

      <div className={styles.total}>
        <span>Total</span>
        <span>{formatPrice(total)}</span>
      </div>
    </div>
  );
};

export default PriceBreakdown;
