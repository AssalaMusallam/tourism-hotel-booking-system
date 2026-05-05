import { useMemo } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { addDays, differenceInDays, format, parseISO } from 'date-fns';
import toast from 'react-hot-toast';
import { Minus, Plus } from 'lucide-react';
import { getRoomTypeById } from '../api/roomsApi';
import { friendlyBookingError } from '../api/bookings';
import { useCreateBooking } from '../hooks/useBookingQueries';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import Spinner from '../components/ui/Spinner';
import styles from './BookingFlowPage.module.css';

const today = format(new Date(), 'yyyy-MM-dd');
const tomorrow = format(addDays(new Date(), 1), 'yyyy-MM-dd');
const phonePattern = /^\+?[0-9\-\s]{7,20}$/;

const money = (value) =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const schema = z.object({
  guestName: z.string().trim().min(2, 'Guest name must be 2-150 characters').max(150, 'Guest name must be 2-150 characters'),
  guestPhone: z.string().trim().regex(phonePattern, 'Use 7-20 digits, spaces, or dashes'),
  adults: z.coerce.number().min(1),
  children: z.coerce.number().min(0),
  checkIn: z.string().min(1, 'Check-in is required'),
  checkOut: z.string().min(1, 'Check-out is required'),
  guestNotes: z.string().max(1000, 'Notes cannot exceed 1000 characters').optional().or(z.literal('')),
}).refine((values) => parseISO(values.checkIn) >= parseISO(today), {
  message: 'Check-in must be today or a future date',
  path: ['checkIn'],
}).refine((values) => parseISO(values.checkOut) > parseISO(values.checkIn), {
  message: 'Check-out must be after check-in',
  path: ['checkOut'],
});

const Stepper = ({ value, onChange, min = 0, label }) => (
  <div className={styles.stepper}>
    <span>{label}</span>
    <div className={styles.stepperControls}>
      <button type="button" onClick={() => onChange(Math.max(min, Number(value || 0) - 1))} aria-label={`Decrease ${label}`}>
        <Minus size={15} />
      </button>
      <strong>{value}</strong>
      <button type="button" onClick={() => onChange(Number(value || 0) + 1)} aria-label={`Increase ${label}`}>
        <Plus size={15} />
      </button>
    </div>
  </div>
);

const BookingFlowPage = () => {
  const { hotelId, roomTypeId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const createMutation = useCreateBooking();

  const { data: roomType, isLoading } = useQuery({
    queryKey: ['rooms', 'detail', roomTypeId],
    queryFn: () => getRoomTypeById(roomTypeId),
    enabled: Boolean(roomTypeId),
    staleTime: 5 * 60 * 1000,
  });

  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      guestName: user?.fullName || '',
      guestPhone: user?.phone || '',
      adults: Number(searchParams.get('adults') || searchParams.get('guests') || 1),
      children: Number(searchParams.get('children') || 0),
      checkIn: searchParams.get('checkIn') || today,
      checkOut: searchParams.get('checkOut') || tomorrow,
      guestNotes: '',
    },
  });

  const watched = form.watch();
  const checkOutMin = useMemo(() => {
    const base = watched.checkIn ? parseISO(watched.checkIn) : new Date();
    return format(addDays(base, 1), 'yyyy-MM-dd');
  }, [watched.checkIn]);

  const pricePreview = useMemo(() => {
    const nights = watched.checkIn && watched.checkOut
      ? Math.max(0, differenceInDays(parseISO(watched.checkOut), parseISO(watched.checkIn)))
      : 0;
    const pricePerNight = Number(roomType?.basePrice || 0);
    const subtotal = nights * pricePerNight;
    return { nights, pricePerNight, totalPrice: subtotal };
  }, [roomType?.basePrice, watched.checkIn, watched.checkOut]);

  const submit = (values) => {
    const payload = {
      roomTypeId: Number(roomTypeId),
      guestName: values.guestName.trim(),
      guestPhone: values.guestPhone.trim(),
      adults: Number(values.adults),
      children: Number(values.children),
      checkIn: values.checkIn,
      checkOut: values.checkOut,
      guestNotes: values.guestNotes?.trim() || undefined,
      // The UI intentionally does not expose email; backend overwrites this from JWT.
      guestEmail: user?.email,
    };

    createMutation.mutate(payload, {
      onSuccess: (booking) => {
        toast.success('Booking created');
        navigate(`/bookings/confirmation/${booking.id}`, { replace: true });
      },
      onError: (error) => toast.error(friendlyBookingError(error)),
    });
  };

  if (isLoading) return <Spinner centered />;

  return (
    <main className={styles.page}>
      <div className="container">
        <div className={styles.headerBlock}>
          <span className={styles.eyebrow}>Guest booking</span>
          <h1>Complete your reservation</h1>
          <p>{roomType?.name || 'Selected room'} at hotel #{hotelId}</p>
        </div>

        <div className={styles.layout}>
          <section className={styles.formCard}>
            <form className={styles.form} onSubmit={form.handleSubmit(submit)}>
              <Input label="Guest name" error={form.formState.errors.guestName?.message} {...form.register('guestName')} />
              <Input label="Phone number" type="tel" placeholder="+970 599 000 000" error={form.formState.errors.guestPhone?.message} {...form.register('guestPhone')} />

              <div className={styles.row}>
                <Input label="Check-in" type="date" min={today} error={form.formState.errors.checkIn?.message} {...form.register('checkIn')} />
                <Input label="Check-out" type="date" min={checkOutMin} error={form.formState.errors.checkOut?.message} {...form.register('checkOut')} />
              </div>

              <div className={styles.row}>
                <Controller
                  control={form.control}
                  name="adults"
                  render={({ field }) => <Stepper label="Adults" min={1} value={field.value} onChange={field.onChange} />}
                />
                <Controller
                  control={form.control}
                  name="children"
                  render={({ field }) => <Stepper label="Children" min={0} value={field.value} onChange={field.onChange} />}
                />
              </div>

              <div className={styles.field}>
                <label className={styles.label}>Guest notes</label>
                <textarea className={styles.textarea} rows={4} maxLength={1000} {...form.register('guestNotes')} />
                {form.formState.errors.guestNotes && <span className={styles.err}>{form.formState.errors.guestNotes.message}</span>}
              </div>

              <Button type="submit" variant="primary" size="lg" fullWidth loading={createMutation.isPending}>
                Create Booking
              </Button>
            </form>
          </section>

          <aside className={styles.summaryCard}>
            <h2>{roomType?.name || 'Room'}</h2>
            <div className={styles.summaryLine}>
              <span>Capacity</span>
              <strong>{roomType?.capacity || 0} guests</strong>
            </div>
            <div className={styles.summaryLine}>
              <span>Price per night</span>
              <strong>{money(pricePreview.pricePerNight)}</strong>
            </div>
            <div className={styles.summaryLine}>
              <span>{money(pricePreview.pricePerNight)} x {pricePreview.nights} night{pricePreview.nights === 1 ? '' : 's'}</span>
              <strong>{money(pricePreview.totalPrice)}</strong>
            </div>
            <div className={styles.totalLine}>
              <span>Total preview</span>
              <strong>{money(pricePreview.totalPrice)}</strong>
            </div>
          </aside>
        </div>
      </div>
    </main>
  );
};

export default BookingFlowPage;
