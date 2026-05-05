import { useState } from 'react';
import { ChevronDown, ChevronUp, X } from 'lucide-react';
import { formatDate } from '../../utils/formatDate';
import { formatPrice } from '../../utils/formatPrice';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Modal from '../ui/Modal';
import styles from './BookingCard.module.css';

const BookingCard = ({ booking, onCancel }) => {
  const [expanded, setExpanded] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);

  const canCancel = booking.status === 'CONFIRMED' || booking.status === 'PENDING';

  return (
    <>
      <div className={styles.card}>
        <div className={styles.header} onClick={() => setExpanded((v) => !v)}>
          <div className={styles.mainInfo}>
            <h4 className={styles.hotelName}>{booking.hotelName}</h4>
            <div className={styles.meta}>
              <span className={styles.ref}>#{booking.id}</span>
              <Badge status={booking.status} />
            </div>
          </div>
          <div className={styles.summary}>
            <div className={styles.dates}>
              <span>{formatDate(booking.checkIn)}</span>
              <span>→</span>
              <span>{formatDate(booking.checkOut)}</span>
            </div>
            <span className={styles.price}>{formatPrice(booking.totalPrice)}</span>
          </div>
          <button className={styles.expandBtn} aria-label="Toggle details">
            {expanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
          </button>
        </div>

        {expanded && (
          <div className={styles.details}>
            <div className={styles.detailRow}>
              <span>Room Type</span>
              <span>{booking.roomType}</span>
            </div>
            <div className={styles.detailRow}>
              <span>Nights</span>
              <span>{booking.nights}</span>
            </div>
            <div className={styles.detailRow}>
              <span>City</span>
              <span>{booking.city}</span>
            </div>
            <div className={styles.detailRow}>
              <span>Booked On</span>
              <span>{formatDate(booking.createdAt)}</span>
            </div>
            {canCancel && (
              <Button
                variant="danger"
                size="sm"
                onClick={() => setShowCancelModal(true)}
                className={styles.cancelBtn}
              >
                <X size={14} /> Cancel Booking
              </Button>
            )}
          </div>
        )}
      </div>

      <Modal
        isOpen={showCancelModal}
        onClose={() => setShowCancelModal(false)}
        title="Cancel Booking"
        size="sm"
      >
        <p className={styles.cancelText}>
          Are you sure you want to cancel booking <strong>#{booking.id}</strong> at{' '}
          <strong>{booking.hotelName}</strong>? This action cannot be undone.
        </p>
        <div className={styles.cancelActions}>
          <Button variant="ghost" onClick={() => setShowCancelModal(false)}>Keep Booking</Button>
          <Button
            variant="danger"
            onClick={() => {
              onCancel(booking.id);
              setShowCancelModal(false);
            }}
          >
            Yes, Cancel
          </Button>
        </div>
      </Modal>
    </>
  );
};

export default BookingCard;
