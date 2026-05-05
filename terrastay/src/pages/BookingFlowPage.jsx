import { useState, useEffect } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle, ChevronRight, Lock } from 'lucide-react';
import toast from 'react-hot-toast';
import { getHotelById } from '../api/hotels';
import { createBooking } from '../api/bookings';
import { getNights, toInputDate } from '../utils/formatDate';
import { formatPrice } from '../utils/formatPrice';
import { addDays } from 'date-fns';
import PriceBreakdown from '../components/booking/PriceBreakdown';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import Spinner from '../components/ui/Spinner';
import useAuth from '../hooks/useAuth';
import useSearchParamsHook from '../hooks/useSearchParams';
import styles from './BookingFlowPage.module.css';

const guestSchema = z.object({
  firstName: z.string().min(2, 'First name required'),
  lastName: z.string().min(2, 'Last name required'),
  email: z.string().email('Valid email required'),
  phone: z.string().min(7, 'Phone number required'),
  specialRequests: z.string().optional(),
});

const paymentSchema = z.object({
  cardNumber: z.string().min(16, 'Enter a valid card number').max(19),
  expiry: z.string().regex(/^\d{2}\/\d{2}$/, 'Format: MM/YY'),
  cvv: z.string().min(3, 'CVV required').max(4),
  cardName: z.string().min(2, 'Cardholder name required'),
});

const STEPS = ['Your Details', 'Payment', 'Confirmation'];

const BookingFlowPage = () => {
  const { hotelId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { getParam } = useSearchParamsHook();
  const [step, setStep] = useState(0);
  const [guestData, setGuestData] = useState(null);
  const [booking, setBooking] = useState(null);

  const checkIn = getParam('checkIn') || toInputDate(addDays(new Date(), 1));
  const checkOut = getParam('checkOut') || toInputDate(addDays(new Date(), 4));
  const passedRoom = location.state?.room;
  const passedHotel = location.state?.hotel;
  const [selectedRoom] = useState(passedRoom);

  const { data: hotel, isLoading } = useQuery({
    queryKey: ['hotel', hotelId],
    queryFn: () => getHotelById(hotelId),
    initialData: passedHotel,
    enabled: !passedHotel,
  });

  useEffect(() => {
    const pending = sessionStorage.getItem('pendingBooking');
    if (pending) sessionStorage.removeItem('pendingBooking');
  }, []);

  const nights = getNights(checkIn, checkOut);
  const roomPrice = selectedRoom?.pricePerNight || hotel?.pricePerNight || 0;
  const taxes = Math.round(roomPrice * nights * 0.12);
  const total = roomPrice * nights + taxes;

  const guestForm = useForm({
    resolver: zodResolver(guestSchema),
    defaultValues: {
      firstName: user?.name?.split(' ')[0] || '',
      lastName: user?.name?.split(' ').slice(1).join(' ') || '',
      email: user?.email || '',
    },
  });

  const paymentForm = useForm({ resolver: zodResolver(paymentSchema) });

  const mutation = useMutation({
    mutationFn: createBooking,
    onSuccess: (data) => {
      setBooking(data);
      setStep(2);
    },
    onError: () => {
      toast.error('Booking failed. Please try again.');
    },
  });

  const handleGuestSubmit = (data) => {
    setGuestData(data);
    setStep(1);
  };

  const handlePaymentSubmit = (payData) => {
    mutation.mutate({
      hotelId,
      hotelName: hotel?.name,
      city: hotel?.city,
      roomId: selectedRoom?.id,
      roomType: selectedRoom?.type || 'Standard Room',
      checkIn,
      checkOut,
      nights,
      totalPrice: total,
      guestName: `${guestData.firstName} ${guestData.lastName}`,
      guestEmail: guestData.email,
      ...guestData,
    });
  };

  const formatCardNumber = (val) => {
    return val.replace(/\D/g, '').slice(0, 16).replace(/(.{4})/g, '$1 ').trim();
  };

  if (isLoading) return <Spinner centered />;

  return (
    <div className={styles.page}>
      <div className="container">
        {/* Step Indicator */}
        <div className={styles.steps}>
          {STEPS.map((s, i) => (
            <div key={s} className={`${styles.stepItem} ${i <= step ? styles.stepActive : ''} ${i < step ? styles.stepDone : ''}`}>
              <div className={styles.stepCircle}>
                {i < step ? <CheckCircle size={16} /> : i + 1}
              </div>
              <span className={styles.stepLabel}>{s}</span>
              {i < STEPS.length - 1 && <ChevronRight size={16} className={styles.stepArrow} />}
            </div>
          ))}
        </div>

        <div className={styles.layout}>
          <AnimatePresence mode="wait">
            {step === 0 && (
              <motion.div
                key="step0"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                className={styles.formSection}
              >
                <h2 className={styles.stepTitle}>Your Details</h2>
                <form onSubmit={guestForm.handleSubmit(handleGuestSubmit)} className={styles.form}>
                  <div className={styles.row}>
                    <Input label="First Name" error={guestForm.formState.errors.firstName?.message} {...guestForm.register('firstName')} />
                    <Input label="Last Name" error={guestForm.formState.errors.lastName?.message} {...guestForm.register('lastName')} />
                  </div>
                  <Input label="Email Address" type="email" error={guestForm.formState.errors.email?.message} {...guestForm.register('email')} />
                  <Input label="Phone Number" type="tel" error={guestForm.formState.errors.phone?.message} {...guestForm.register('phone')} placeholder="+970 / +972 ..." />
                  <div className={styles.field}>
                    <label className={styles.label}>Special Requests (optional)</label>
                    <textarea className={styles.textarea} rows={3} placeholder="Late check-in, dietary requirements, etc." {...guestForm.register('specialRequests')} />
                  </div>
                  <Button type="submit" variant="primary" size="lg" fullWidth>
                    Continue to Payment
                  </Button>
                </form>
              </motion.div>
            )}

            {step === 1 && (
              <motion.div
                key="step1"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                className={styles.formSection}
              >
                <h2 className={styles.stepTitle}>Payment Details</h2>
                <div className={styles.securityBadge}>
                  <Lock size={14} />
                  <span>Your payment information is 100% secure and encrypted</span>
                </div>
                <form onSubmit={paymentForm.handleSubmit(handlePaymentSubmit)} className={styles.form}>
                  <Input
                    label="Card Number"
                    placeholder="1234 5678 9012 3456"
                    maxLength={19}
                    error={paymentForm.formState.errors.cardNumber?.message}
                    {...paymentForm.register('cardNumber')}
                    onChange={(e) => {
                      const formatted = formatCardNumber(e.target.value);
                      paymentForm.setValue('cardNumber', formatted);
                    }}
                  />
                  <Input label="Cardholder Name" placeholder="AHMAD KHALIL" error={paymentForm.formState.errors.cardName?.message} {...paymentForm.register('cardName')} />
                  <div className={styles.row}>
                    <Input label="Expiry (MM/YY)" placeholder="06/27" maxLength={5} error={paymentForm.formState.errors.expiry?.message} {...paymentForm.register('expiry')} />
                    <Input label="CVV" placeholder="123" maxLength={4} error={paymentForm.formState.errors.cvv?.message} {...paymentForm.register('cvv')} />
                  </div>
                  <div className={styles.btnRow}>
                    <Button type="button" variant="ghost" onClick={() => setStep(0)}>← Back</Button>
                    <Button type="submit" variant="primary" size="lg" loading={mutation.isPending}>
                      Confirm & Pay {formatPrice(total)}
                    </Button>
                  </div>
                </form>
              </motion.div>
            )}

            {step === 2 && (
              <motion.div
                key="step2"
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className={styles.confirmation}
              >
                <motion.div
                  className={styles.successIcon}
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ delay: 0.2, type: 'spring', stiffness: 200 }}
                >
                  <CheckCircle size={56} />
                </motion.div>
                <h2 className={styles.confirmTitle}>Booking Confirmed!</h2>
                <p className={styles.confirmSub}>تم الحجز بنجاح — Your booking is confirmed</p>
                <div className={styles.bookingRef}>
                  Booking Reference: <strong>{booking?.id}</strong>
                </div>
                <div className={styles.confirmDetails}>
                  <div className={styles.confirmRow}><span>Hotel</span><strong>{hotel?.name}</strong></div>
                  <div className={styles.confirmRow}><span>Room</span><strong>{selectedRoom?.type || 'Standard Room'}</strong></div>
                  <div className={styles.confirmRow}><span>Check-in</span><strong>{checkIn}</strong></div>
                  <div className={styles.confirmRow}><span>Check-out</span><strong>{checkOut}</strong></div>
                  <div className={styles.confirmRow}><span>Total Paid</span><strong className={styles.totalPaid}>{formatPrice(total)}</strong></div>
                </div>
                <div className={styles.confirmBtns}>
                  <Button variant="secondary" onClick={() => navigate('/my-bookings')}>View My Bookings</Button>
                  <Button variant="primary" onClick={() => navigate('/')}>Back to Home</Button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {step < 2 && (
            <aside className={styles.sidebar}>
              <PriceBreakdown hotel={hotel} room={selectedRoom || hotel?.rooms?.[0]} checkIn={checkIn} checkOut={checkOut} />
            </aside>
          )}
        </div>
      </div>
    </div>
  );
};

export default BookingFlowPage;
