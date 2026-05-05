import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { CalendarCheck } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { getMyBookings, cancelBooking } from '../api/bookings';
import BookingCard from '../components/booking/BookingCard';
import EmptyState from '../components/ui/EmptyState';
import Spinner from '../components/ui/Spinner';
import { useNavigate } from 'react-router-dom';
import styles from './MyBookingsPage.module.css';

const MyBookingsPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: bookings = [], isLoading } = useQuery({
    queryKey: ['my-bookings'],
    queryFn: getMyBookings,
  });

  const cancelMutation = useMutation({
    mutationFn: cancelBooking,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
      toast.success('Booking cancelled successfully');
    },
    onError: () => {
      toast.error('Failed to cancel booking. Please try again.');
    },
  });

  return (
    <div className="container">
      <div className={styles.header}>
        <h1>My Bookings</h1>
        <p>Manage your hotel reservations across Palestine</p>
      </div>

      {isLoading ? (
        <Spinner centered />
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={CalendarCheck}
          title="No bookings yet"
          description="Start exploring amazing hotels across the Holy Land and make your first reservation."
          actionLabel="Find Hotels"
          onAction={() => navigate('/search')}
        />
      ) : (
        <div className={styles.list}>
          {bookings.map((booking, i) => (
            <motion.div
              key={booking.id}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.06 }}
            >
              <BookingCard
                booking={booking}
                onCancel={(id) => cancelMutation.mutate(id)}
              />
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyBookingsPage;
