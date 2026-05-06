import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { CreditCard, ShieldCheck, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { friendlyPaymentError } from '../api/payments';
import { useBooking } from '../hooks/useBookingQueries';
import { usePayNow, useSimulatePaymentFailure } from '../hooks/usePaymentQueries';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Spinner from '../components/ui/Spinner';
import styles from './PaymentPage.module.css';

const money = (value) =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const PaymentPage = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [cardholderName, setCardholderName] = useState('');
  const [failedPayment, setFailedPayment] = useState(null);
  const { data: booking, isLoading, isError } = useBooking(bookingId);
  const payNow = usePayNow();
  const failPayment = useSimulatePaymentFailure();

  const amount = Number(booking?.totalPrice || 0);

  const handlePay = () => {
    setFailedPayment(null);
    payNow.mutate({ bookingId: Number(bookingId), amount }, {
      onSuccess: (payment) => {
        toast.success('Payment processed');
        navigate(`/payments/${payment.id}/receipt`);
      },
      onError: (error) => toast.error(friendlyPaymentError(error)),
    });
  };

  const handleFailure = () => {
    setFailedPayment(null);
    failPayment.mutate({ bookingId: Number(bookingId), amount, reason: 'Insufficient funds' }, {
      onSuccess: (payment) => setFailedPayment(payment),
      onError: (error) => toast.error(friendlyPaymentError(error)),
    });
  };

  if (isLoading) return <Spinner centered />;
  if (isError || !booking) {
    return <main className={styles.page}><div className={styles.panel}>Booking not found.</div></main>;
  }

  return (
    <main className={styles.page}>
      <div className={styles.layout}>
        <section className={styles.panel}>
          <span className={styles.eyebrow}>Mock payment</span>
          <h1>Pay for booking #{booking.id}</h1>
          <p className={styles.muted}>This is a UI simulation. No real card is charged.</p>

          <div className={styles.cardVisual}>
            <div className={styles.cardTop}>
              <CreditCard size={28} />
              <span>MOCK_CARD</span>
            </div>
            <div className={styles.cardNumber}>**** **** **** 4242</div>
            <div className={styles.cardBottom}>
              <span>EXP 12/26</span>
              <span>CVV ***</span>
            </div>
          </div>

          <Input
            label="Cardholder name"
            value={cardholderName}
            onChange={(event) => setCardholderName(event.target.value)}
            placeholder="Cosmetic only"
          />

          {failedPayment && (
            <div className={styles.failure}>
              <XCircle size={20} />
              <div>
                <strong>Payment failed</strong>
                <p>{failedPayment.failureReason || 'Insufficient funds'}</p>
              </div>
            </div>
          )}

          <div className={styles.actions}>
            <Button variant="primary" size="lg" onClick={handlePay} loading={payNow.isPending}>
              Pay Now
            </Button>
            <Button variant="secondary" onClick={handleFailure} loading={failPayment.isPending}>
              Simulate Failure
            </Button>
          </div>
        </section>

        <aside className={styles.summary}>
          <ShieldCheck size={24} />
          <h2>{money(amount)} USD</h2>
          <div className={styles.line}><span>Hotel</span><strong>{booking.hotelName}</strong></div>
          <div className={styles.line}><span>Room</span><strong>{booking.roomTypeNameEn || booking.roomTypeName}</strong></div>
          <div className={styles.line}><span>Dates</span><strong>{booking.checkIn} to {booking.checkOut}</strong></div>
          <div className={styles.line}><span>Provider</span><strong>MOCK_GATEWAY</strong></div>
        </aside>
      </div>
    </main>
  );
};

export default PaymentPage;
