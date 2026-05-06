import { Link, useParams } from 'react-router-dom';
import { CheckCircle2 } from 'lucide-react';
import { friendlyBookingError } from '../api/bookings';
import { useBooking } from '../hooks/useBookingQueries';
import Button from '../components/ui/Button';
import Spinner from '../components/ui/Spinner';
import StatusBadge from '../components/booking/StatusBadge';
import PriceDisplay from '../components/PriceDisplay';
import useLanguage from '../hooks/useLanguage';
import styles from './BookingConfirmationPage.module.css';

const money = (value) =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const Row = ({ label, value }) => (
  <div className={styles.row}>
    <span>{label}</span>
    <strong>{value ?? '-'}</strong>
  </div>
);

const BookingConfirmationPage = () => {
  const { id } = useParams();
  const { t } = useLanguage();
  const { data: booking, isLoading, isError, error } = useBooking(id);

  if (isLoading) return <Spinner centered />;

  if (isError) {
    return (
      <main className={styles.page}>
        <div className={styles.receipt}>
          <h1>{friendlyBookingError(error)}</h1>
          <Button variant="primary" onClick={() => history.back()}>{t('back')}</Button>
        </div>
      </main>
    );
  }

  return (
    <main className={styles.page}>
      <section className={styles.receipt}>
        <div className={styles.success}>
          <CheckCircle2 size={46} />
        </div>
        <div className={styles.heading}>
          <span>{t('bookingReference')}</span>
          <h1>#{booking.id}</h1>
          <StatusBadge status={booking.status} />
        </div>

        <div className={styles.grid}>
          <Row label={t('hotels')} value={booking.hotelNameEn || booking.hotelName} />
          <Row label={t('roomType')} value={booking.roomTypeNameEn || booking.roomTypeName} />
          <Row label="Room type ID" value={booking.roomTypeId} />
          <Row label={t('guestName')} value={booking.guestName} />
          <Row label="Guest email" value={booking.guestEmail} />
          <Row label="Guest phone" value={booking.guestPhone} />
          <Row label="Adults" value={booking.adults} />
          <Row label="Children" value={booking.children} />
          <Row label="Total guests" value={booking.totalGuests} />
          <Row label={t('checkInDate')} value={booking.checkIn} />
          <Row label={t('checkOutDate')} value={booking.checkOut} />
          <Row label={t('totalNights')} value={booking.nights} />
          <Row label="Created" value={booking.createdAt} />
          <Row label="Updated" value={booking.updatedAt} />
          <Row label="Remaining units" value={booking.remainingUnits} />
          <Row label="Cancelled at" value={booking.cancelledAt} />
          <Row label="Cancellation reason" value={booking.cancellationReason} />
          <Row label={t('refundAmount')} value={booking.refundAmount ? money(booking.refundAmount) : '-'} />
          <Row label="Guest notes" value={booking.guestNotes || '-'} />
        </div>

        <div className={styles.breakdown}>
          <div>
            <span>{money(booking.pricePerNight)} x {booking.nights} night{booking.nights === 1 ? '' : 's'}</span>
            <strong><PriceDisplay usdAmount={Number(booking.pricePerNight) * Number(booking.nights || 0)} showOriginal size="sm" /></strong>
          </div>
          <div className={styles.total}>
            <span>{t('grandTotal')}</span>
            <strong><PriceDisplay usdAmount={booking.totalPrice} showOriginal size="md" /></strong>
          </div>
          <p className={styles.currencyNote}>Exchange rate note: converted prices use the currently selected display currency. Final payment remains in USD.</p>
        </div>

        <Link to="/bookings/my">
          <Button variant="primary" size="lg" fullWidth>{t('myBookings')}</Button>
        </Link>
        <Link to={`/bookings/${booking.id}/pay`}>
          <Button variant="secondary" size="lg" fullWidth>{t('payNow')}</Button>
        </Link>
      </section>
    </main>
  );
};

export default BookingConfirmationPage;
