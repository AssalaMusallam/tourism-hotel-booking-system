import { differenceInDays, format, parseISO } from 'date-fns';
import { TrendingUp } from 'lucide-react';
import SectionError from '../../ui/SectionError';
import { parseApiError } from '../../../lib/parseApiError';
import styles from './RevenueReportCard.module.css';

const money = (v) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(v || 0));

const fmtDate = (str) => {
  try { return format(parseISO(str), 'MMM d, yyyy'); }
  catch { return str; }
};

const Skeleton = () => (
  <div className={styles.card}>
    <div className={styles.cardHeader}>
      <div className="skeleton" style={{ height: 20, width: 180 }} />
      <div className="skeleton" style={{ height: 14, width: 240, marginTop: 6 }} />
    </div>
    <div className={styles.divider} />
    <div className={styles.stats}>
      <div className={styles.stat}>
        <div className="skeleton" style={{ height: 44, width: 140 }} />
        <div className="skeleton" style={{ height: 14, width: 100, marginTop: 8 }} />
      </div>
      <div className={styles.statDivider} />
      <div className={styles.stat}>
        <div className="skeleton" style={{ height: 44, width: 70 }} />
        <div className="skeleton" style={{ height: 14, width: 80, marginTop: 8 }} />
      </div>
    </div>
    <div className={styles.divider} />
    <div className="skeleton" style={{ height: 14, width: '60%' }} />
  </div>
);

/**
 * Displays the revenue report card with total revenue, booking count,
 * average per booking, and period length.
 */
const RevenueReportCard = ({ data, isLoading, isError, error, onRetry }) => {
  if (isLoading) return <Skeleton />;

  if (isError) {
    return (
      <div className={styles.card}>
        <SectionError message={parseApiError(error).message} onRetry={onRetry} />
      </div>
    );
  }

  if (!data) return null;

  const periodDays = differenceInDays(parseISO(data.to), parseISO(data.from));
  const avg = data.totalBookings > 0 ? data.totalRevenue / data.totalBookings : null;
  const isEmpty = data.totalRevenue === 0 && data.totalBookings === 0;

  return (
    <div className={styles.card}>
      <div className={styles.cardHeader}>
        <div className={styles.titleRow}>
          <TrendingUp size={18} className={styles.icon} />
          <h2 className={styles.title}>Revenue Report</h2>
        </div>
        <p className={styles.subtitle}>
          {data.hotelName} &middot; {fmtDate(data.from)} → {fmtDate(data.to)}
        </p>
      </div>

      <div className={styles.divider} />

      {isEmpty ? (
        <div className={styles.emptyState}>
          <span className={styles.emptyArt}>$0</span>
          <p>No confirmed bookings in this period</p>
        </div>
      ) : (
        <div className={styles.stats}>
          <div className={styles.stat}>
            <span className={styles.revenueValue}>{money(data.totalRevenue)}</span>
            <span className={styles.statLabel}>Total Revenue</span>
          </div>
          <div className={styles.statDivider} />
          <div className={styles.stat}>
            <span className={styles.bookingValue}>{data.totalBookings}</span>
            <span className={styles.statLabel}>Bookings</span>
          </div>
        </div>
      )}

      <div className={styles.divider} />

      <div className={styles.footer}>
        <span>Avg. revenue per booking: <strong>{avg !== null ? money(avg) : '—'}</strong></span>
        <span>Period: <strong>{periodDays} day{periodDays === 1 ? '' : 's'}</strong></span>
      </div>
    </div>
  );
};

export default RevenueReportCard;
