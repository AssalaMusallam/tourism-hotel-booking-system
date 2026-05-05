import { Link, useParams } from 'react-router-dom';
import { CheckCircle2 } from 'lucide-react';
import { friendlyBookingError } from '../api/bookings';
import { useBooking } from '../hooks/useBookingQueries';
import Button from '../components/ui/Button';
import Spinner from '../components/ui/Spinner';
import StatusBadge from '../components/booking/StatusBadge';
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
  const { data: booking, isLoading, isError, error } = useBooking(id);

  if (isLoading) return <Spinner centered />;

  if (isError) {
    return (
      <main className={styles.page}>
        <div className={styles.receipt}>
          <h1>{friendlyBookingError(error)}</h1>
          <Button variant="primary" onClick={() => history.back()}>Go Back</Button>
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
          <span>Booking receipt</span>
          <h1>Reservation #{booking.id}</h1>
          <StatusBadge status={booking.status} />
        </div>

        <div className={styles.grid}>
          <Row label="Hotel" value={booking.hotelName} />
          <Row label="Room" value={booking.roomTypeName} />
          <Row label="Room type ID" value={booking.roomTypeId} />
          <Row label="Guest name" value={booking.guestName} />
          <Row label="Guest email" value={booking.guestEmail} />
          <Row label="Guest phone" value={booking.guestPhone} />
          <Row label="Adults" value={booking.adults} />
          <Row label="Children" value={booking.children} />
          <Row label="Total guests" value={booking.totalGuests} />
          <Row label="Check-in" value={booking.checkIn} />
          <Row label="Check-out" value={booking.checkOut} />
          <Row label="Nights" value={booking.nights} />
          <Row label="Created" value={booking.createdAt} />
          <Row label="Updated" value={booking.updatedAt} />
          <Row label="Remaining units" value={booking.remainingUnits} />
          <Row label="Cancelled at" value={booking.cancelledAt} />
          <Row label="Cancellation reason" value={booking.cancellationReason} />
          <Row label="Refund amount" value={booking.refundAmount ? money(booking.refundAmount) : '-'} />
          <Row label="Guest notes" value={booking.guestNotes || '-'} />
        </div>

        <div className={styles.breakdown}>
          <div>
            <span>{money(booking.pricePerNight)} x {booking.nights} night{booking.nights === 1 ? '' : 's'}</span>
            <strong>{money(Number(booking.pricePerNight) * Number(booking.nights || 0))}</strong>
          </div>
          <div className={styles.total}>
            <span>Total</span>
            <strong>{money(booking.totalPrice)}</strong>
          </div>
        </div>

        <Link to="/bookings/my">
          <Button variant="primary" size="lg" fullWidth>View My Bookings</Button>
        </Link>
        <Link to={`/bookings/${booking.id}/pay`}>
          <Button variant="secondary" size="lg" fullWidth>Pay Now</Button>
        </Link>
      </section>
    </main>
  );
};

export default BookingConfirmationPage;
