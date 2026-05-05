import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { friendlyPaymentError } from '../api/payments';
import { useLatestPayment, usePaymentHistory, useRefundPayment } from '../hooks/usePaymentQueries';
import Button from '../components/ui/Button';
import Spinner from '../components/ui/Spinner';
import PaymentStatusBadge from '../components/payment/PaymentStatusBadge';
import RefundModal from '../components/payment/RefundModal';
import styles from './PaymentHistoryPage.module.css';

const money = (value, currency = 'USD') =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency });

const PaymentHistoryPage = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [refundOpen, setRefundOpen] = useState(false);
  const { data: history = [], isLoading } = usePaymentHistory(bookingId);
  const { data: latest } = useLatestPayment(bookingId);
  const refundMutation = useRefundPayment();
  const latestPayment = latest || history[0];

  const handleRefund = (reason, reset) => {
    refundMutation.mutate({ id: latestPayment.id, reason }, {
      onSuccess: (payment) => {
        reset?.();
        setRefundOpen(false);
        toast.success(`Refund processed: ${money(payment.amount, payment.currency)}`);
      },
      onError: (error) => toast.error(friendlyPaymentError(error)),
    });
  };

  if (isLoading) return <Spinner centered />;

  return (
    <main className={styles.page}>
      <section className={styles.panel}>
        <div className={styles.header}>
          <div>
            <span className={styles.eyebrow}>Payment history</span>
            <h1>Booking #{bookingId}</h1>
          </div>
          <div className={styles.headerActions}>
            {latestPayment?.status === 'SUCCESS' && (
              <Button variant="primary" onClick={() => setRefundOpen(true)}>Request Refund</Button>
            )}
            <Button variant="secondary" onClick={() => navigate(`/bookings/${bookingId}/pay`)}>New Attempt</Button>
          </div>
        </div>

        {history.length === 0 ? (
          <div className={styles.empty}>
            <h2>No payment attempts yet</h2>
            <p>Create a mock payment for this booking.</p>
            <Button variant="primary" onClick={() => navigate(`/bookings/${bookingId}/pay`)}>Pay Now</Button>
          </div>
        ) : (
          <div className={styles.timeline}>
            {history.map((payment, index) => (
              <article key={payment.id} className={[styles.item, index === 0 ? styles.latest : ''].filter(Boolean).join(' ')}>
                <div className={styles.dot} />
                <div className={styles.itemBody}>
                  <div className={styles.itemTop}>
                    <PaymentStatusBadge status={payment.status} />
                    {index === 0 && <span className={styles.latestLabel}>Most recent</span>}
                  </div>
                  <Link to={`/payments/${payment.id}/receipt`} className={styles.ref}>
                    {payment.transactionReference}
                  </Link>
                  <div className={styles.meta}>
                    <span>{money(payment.amount, payment.currency)} {payment.currency}</span>
                    <span>{payment.createdAt}</span>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <RefundModal
        isOpen={refundOpen}
        onClose={() => setRefundOpen(false)}
        payment={latestPayment}
        onRefund={handleRefund}
        loading={refundMutation.isPending}
      />
    </main>
  );
};

export default PaymentHistoryPage;
