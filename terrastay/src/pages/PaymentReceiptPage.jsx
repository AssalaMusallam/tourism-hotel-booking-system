import { Link, useNavigate, useParams } from 'react-router-dom';
import { CheckCircle2, XCircle, RotateCcw, Clock } from 'lucide-react';
import { format } from 'date-fns';
import { usePayment } from '../hooks/usePaymentQueries';
import Button from '../components/ui/Button';
import Spinner from '../components/ui/Spinner';
import PaymentStatusBadge from '../components/payment/PaymentStatusBadge';
import useLanguage from '../hooks/useLanguage';
import styles from './PaymentReceiptPage.module.css';

const money = (value, currency = 'USD') =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency });

const formatTime = (value) => value ? format(new Date(value), 'MMMM d, yyyy \'at\' h:mm a') : '-';

const iconFor = {
  SUCCESS: CheckCircle2,
  FAILED: XCircle,
  REFUNDED: RotateCcw,
  PENDING: Clock,
};

const PaymentReceiptPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useLanguage();
  const { data: payment, isLoading, isError } = usePayment(id);

  if (isLoading) return <Spinner centered />;
  if (isError || !payment) return <main className={styles.page}><section className={styles.receipt}>{t('noData')}</section></main>;

  const Icon = iconFor[payment.status] || Clock;

  return (
    <main className={styles.page}>
      <section className={styles.receipt}>
        <div className={[styles.iconWrap, styles[payment.status?.toLowerCase()]].filter(Boolean).join(' ')}>
          <Icon size={48} />
        </div>
        <div className={styles.heading}>
          <h1>{payment.status === 'SUCCESS' ? t('paymentSuccess') : payment.status === 'FAILED' ? t('paymentFailed') : t('downloadReceipt')}</h1>
          <PaymentStatusBadge status={payment.status} />
        </div>

        {payment.status === 'FAILED' && (
          <div className={styles.alert}>{payment.failureReason || 'Payment failed'}</div>
        )}

        <div className={styles.rows}>
          <div><span>{t('receiptNumber')}</span><strong className={styles.mono}>{payment.transactionReference}</strong></div>
          <div><span>{t('totalPrice')}</span><strong>{money(payment.amount, payment.currency)} {payment.currency}</strong></div>
          <div><span>Provider</span><strong>{payment.providerName || 'MOCK_GATEWAY'}</strong></div>
          <div><span>{t('paymentMethod')}</span><strong>{payment.method}</strong></div>
          <div><span>Paid at</span><strong>{formatTime(payment.paidAt)}</strong></div>
          <div><span>Created at</span><strong>{formatTime(payment.createdAt)}</strong></div>
          {payment.status === 'REFUNDED' && (
            <>
              <div><span>{t('paymentRefunded')}</span><strong>{formatTime(payment.refundedAt)}</strong></div>
              <div><span>Refund reason</span><strong>{payment.refundReason || '-'}</strong></div>
            </>
          )}
          <div>
            <span>{t('bookingReference')}</span>
            <Link to={`/bookings/confirmation/${payment.bookingId}`} className={styles.link}>#{payment.bookingId}</Link>
          </div>
        </div>

        <div className={styles.actions}>
          {payment.status === 'FAILED' && (
            <Button variant="primary" onClick={() => navigate(`/bookings/${payment.bookingId}/pay`)}>
              {t('retry')}
            </Button>
          )}
          <Button variant="secondary" onClick={() => navigate(`/bookings/${payment.bookingId}/payments`)}>
            Payment History
          </Button>
        </div>
      </section>
    </main>
  );
};

export default PaymentReceiptPage;
