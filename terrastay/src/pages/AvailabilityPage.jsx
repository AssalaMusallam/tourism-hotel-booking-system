import { useMemo } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { addDays, format, parseISO } from 'date-fns';
import useAvailability from '../hooks/useAvailability';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import SectionError from '../components/ui/SectionError';
import RoomCard, { RoomCardSkeleton, getAvailabilityRoomId } from '../components/availability/RoomCard';
import { parseApiError } from '../lib/parseApiError';
import styles from './AvailabilityPage.module.css';

const today = format(new Date(), 'yyyy-MM-dd');
const tomorrow = format(addDays(new Date(), 1), 'yyyy-MM-dd');

const schema = z.object({
  checkIn: z.string().min(1, 'Check-in is required'),
  checkOut: z.string().min(1, 'Check-out is required'),
  guests: z.coerce.number().min(1, 'At least 1 guest').max(20, 'Maximum 20 guests'),
}).refine((data) => parseISO(data.checkOut) > parseISO(data.checkIn), {
  message: 'Check-out must be after check-in',
  path: ['checkOut'],
});

const AvailabilityPage = () => {
  const { hotelId: routeHotelId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const hotelId = routeHotelId || searchParams.get('hotelId');
  const checkIn = searchParams.get('checkIn') || today;
  const checkOut = searchParams.get('checkOut') || tomorrow;
  const guests = searchParams.get('guests') || '2';

  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: { checkIn, checkOut, guests: Number(guests) },
  });

  const params = useMemo(() => ({
    checkIn,
    checkOut,
    guests: Number(guests || 1),
  }), [checkIn, checkOut, guests]);

  const { data, isLoading, isError, error, refetch } = useAvailability(
    hotelId,
    params.checkIn,
    params.checkOut,
    params.guests
  );

  const rooms = data?.content || [];

  const submitSearch = (values) => {
    const next = new URLSearchParams(searchParams);
    next.set('checkIn', values.checkIn);
    next.set('checkOut', values.checkOut);
    next.set('guests', String(values.guests));
    if (!routeHotelId && hotelId) next.set('hotelId', hotelId);
    setSearchParams(next);
  };

  const handleBook = (room) => {
    const pendingBooking = {
      hotelId: String(hotelId),
      roomTypeId: getAvailabilityRoomId(room),
      roomName: room.roomTypeName,
      checkIn: params.checkIn,
      checkOut: params.checkOut,
      guests: params.guests,
    };

    if (!isAuthenticated) {
      sessionStorage.setItem('pendingBooking', JSON.stringify(pendingBooking));
      toast('Please log in to complete your booking');
      navigate('/login', { state: { from: `/hotels/${hotelId}/availability?${searchParams.toString()}` } });
      return;
    }

    const bookingParams = new URLSearchParams({
      checkIn: params.checkIn,
      checkOut: params.checkOut,
      guests: String(params.guests),
    });
    navigate(`/hotels/${hotelId}/rooms/${getAvailabilityRoomId(room)}/book?${bookingParams.toString()}`, { state: { pendingBooking, room } });
  };

  return (
    <main className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroInner}>
          <div>
            <span className={styles.eyebrow}>Availability & quote</span>
            <h1 className={styles.title}>Choose an available room</h1>
            <p className={styles.subtitle}>
              Review live room availability and a clear quote for your selected stay before booking.
            </p>
          </div>

          <form className={styles.form} onSubmit={form.handleSubmit(submitSearch)}>
            <Input
              label="Check-in"
              type="date"
              min={today}
              error={form.formState.errors.checkIn?.message}
              {...form.register('checkIn')}
            />
            <Input
              label="Check-out"
              type="date"
              min={today}
              error={form.formState.errors.checkOut?.message}
              {...form.register('checkOut')}
            />
            <Input
              label="Guests"
              type="number"
              min="1"
              max="20"
              error={form.formState.errors.guests?.message}
              {...form.register('guests')}
            />
            <Button className={styles.formButton} type="submit" variant="primary">
              Update availability
            </Button>
          </form>
        </div>
      </section>

      <section className={styles.content}>
        <div className={styles.summary}>
          <span>
            <strong>{rooms.length}</strong> room option{rooms.length === 1 ? '' : 's'} for {params.guests} guest{params.guests === 1 ? '' : 's'}
          </span>
          <span>{params.checkIn} to {params.checkOut}</span>
        </div>

        {isLoading && (
          <div className={styles.list}>
            {Array.from({ length: 3 }).map((_, index) => <RoomCardSkeleton key={index} />)}
          </div>
        )}

        {!isLoading && isError && (
          <SectionError message={parseApiError(error).message} onRetry={refetch} />
        )}

        {!isLoading && !isError && rooms.length === 0 && (
          <div className={styles.empty}>
            <div>
              <div className={styles.emptyArt}>0</div>
              <h2>No rooms available for selected dates</h2>
              <p>Try different dates or reduce the number of guests.</p>
            </div>
          </div>
        )}

        {!isLoading && !isError && rooms.length > 0 && (
          <div className={styles.list}>
            {rooms.map((room) => (
              <RoomCard
                key={getAvailabilityRoomId(room)}
                room={room}
                checkIn={params.checkIn}
                checkOut={params.checkOut}
                onBook={handleBook}
              />
            ))}
          </div>
        )}
      </section>
    </main>
  );
};

export default AvailabilityPage;
