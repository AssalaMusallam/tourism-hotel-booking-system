import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CalendarCheck } from 'lucide-react';
import toast from 'react-hot-toast';
import { friendlyBookingError } from '../api/bookings';
import { useCancelBooking, useMyBookings } from '../hooks/useBookingQueries';
import BookingCard from '../components/booking/BookingCard';
import CancelBookingModal from '../components/booking/CancelBookingModal';
import EmptyState from '../components/ui/EmptyState';
import Pagination from '../components/ui/Pagination';
import Spinner from '../components/ui/Spinner';
import styles from './MyBookingsPage.module.css';

const tabs = [
  { label: 'All', value: '' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'Cancelled', value: 'CANCELLED' },
  { label: 'Completed', value: 'COMPLETED' },
];

const MyBookingsPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [cancelTarget, setCancelTarget] = useState(null);
  const status = searchParams.get('status') || '';
  const page = Number(searchParams.get('page') || 0);

  const params = {
    page,
    size: 10,
    sort: 'checkIn,desc',
    ...(status && { status }),
  };

  const { data, isLoading } = useMyBookings(params);
  const cancelMutation = useCancelBooking();
  const bookings = data?.content || [];
  const totalPages = data?.totalPages || 0;

  const setFilter = (nextStatus) => {
    const next = new URLSearchParams(searchParams);
    if (nextStatus) next.set('status', nextStatus);
    else next.delete('status');
    next.set('page', '0');
    setSearchParams(next);
  };

  const setPage = (nextPage) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(nextPage));
    setSearchParams(next);
  };

  const confirmCancel = (reason, reset) => {
    cancelMutation.mutate({ id: cancelTarget.id, reason }, {
      onSuccess: (booking) => {
        reset?.();
        setCancelTarget(null);
        const refund = booking.refundAmount
          ? ` Refund: ${Number(booking.refundAmount).toLocaleString('en-US', { style: 'currency', currency: 'USD' })}`
          : '';
        toast.success(`Booking cancelled.${refund}`);
      },
      onError: (error) => toast.error(friendlyBookingError(error)),
    });
  };

  return (
    <main className="container">
      <div className={styles.header}>
        <h1>My Bookings</h1>
        <p>Review upcoming, completed, and cancelled reservations.</p>
      </div>

      <div className={styles.tabs}>
        {tabs.map((tab) => (
          <button
            key={tab.label}
            className={[styles.tab, tab.value === status ? styles.activeTab : ''].filter(Boolean).join(' ')}
            onClick={() => setFilter(tab.value)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {isLoading ? (
        <Spinner centered />
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={CalendarCheck}
          title="No bookings found"
          description="When you book a room, it will appear here."
          action={{ label: 'Find Hotels', onClick: () => navigate('/search') }}
        />
      ) : (
        <>
          <div className={styles.list}>
            {bookings.map((booking) => (
              <BookingCard
                key={booking.id}
                booking={booking}
                onCancel={setCancelTarget}
                onReview={(item) => navigate(`/bookings/${item.id}/review`)}
              />
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <CancelBookingModal
        isOpen={Boolean(cancelTarget)}
        booking={cancelTarget}
        onClose={() => setCancelTarget(null)}
        onConfirm={confirmCancel}
        loading={cancelMutation.isPending}
      />
    </main>
  );
};

export default MyBookingsPage;
