import { useParams } from 'react-router-dom';
import { useBooking } from '../hooks/useBookingQueries';
import ReviewForm from '../components/review/ReviewForm';
import Spinner from '../components/ui/Spinner';
import styles from './ReviewPage.module.css';

const ReviewPage = () => {
  const { bookingId } = useParams();
  const { data: booking, isLoading, isError } = useBooking(bookingId);

  if (isLoading) return <Spinner centered />;

  if (isError || !booking) {
    return (
      <main className={styles.page}>
        <section className={styles.message}>Booking not found.</section>
      </main>
    );
  }

  if (booking.status !== 'COMPLETED') {
    return (
      <main className={styles.page}>
        <section className={styles.message}>Your stay must be completed before leaving a review.</section>
      </main>
    );
  }

  return (
    <main className={styles.page}>
      <ReviewForm
        bookingId={booking.id}
        guestEmail={booking.guestEmail}
        hotelName={booking.hotelName}
        checkOut={booking.checkOut}
      />
    </main>
  );
};

export default ReviewPage;
