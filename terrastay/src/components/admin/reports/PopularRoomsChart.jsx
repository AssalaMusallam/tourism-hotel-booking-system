import { useEffect, useState } from 'react';
import { Trophy } from 'lucide-react';
import SectionError from '../../ui/SectionError';
import { parseApiError } from '../../../lib/parseApiError';
import styles from './PopularRoomsChart.module.css';

const BAR_COLORS = ['#fbbf24', '#94a3b8', '#c2410c'];
const DEFAULT_COLOR = '#818cf8';
const RANK_LABELS = ['#1', '#2', '#3'];

const barColor = (index) => BAR_COLORS[index] ?? DEFAULT_COLOR;

const BarSkeleton = () => (
  <div className={styles.card}>
    <div className={styles.cardHeader}>
      <div className="skeleton" style={{ height: 20, width: 220 }} />
    </div>
    <div className={styles.divider} />
    <div className={styles.barsWrap}>
      {[80, 55, 35].map((w, i) => (
        <div key={i} className={styles.row}>
          <div className="skeleton" style={{ height: 14, width: 120 }} />
          <div className={styles.trackWrap}>
            <div className="skeleton" style={{ height: 28, width: `${w}%` }} />
          </div>
          <div className="skeleton" style={{ height: 14, width: 36 }} />
        </div>
      ))}
    </div>
  </div>
);

/**
 * Horizontal bar chart showing most popular room types by booking count.
 * Bars animate from 0 → actual width on mount (600ms ease-out).
 */
const PopularRoomsChart = ({ data, isLoading, isError, error, onRetry }) => {
  const [animated, setAnimated] = useState(false);

  useEffect(() => {
    if (data?.length) {
      setAnimated(false);
      const t = setTimeout(() => setAnimated(true), 80);
      return () => clearTimeout(t);
    }
  }, [data]);

  if (isLoading) return <BarSkeleton />;

  if (isError) {
    return (
      <div className={styles.card}>
        <SectionError message={parseApiError(error).message} onRetry={onRetry} />
      </div>
    );
  }

  const isEmpty = !data || data.length === 0;
  const maxCount = isEmpty ? 1 : Math.max(...data.map((d) => d.bookingsCount));

  return (
    <div className={styles.card}>
      <div className={styles.cardHeader}>
        <div className={styles.titleRow}>
          <Trophy size={18} className={styles.icon} />
          <h2 className={styles.title}>Most Popular Room Types</h2>
        </div>
      </div>

      <div className={styles.divider} />

      {isEmpty ? (
        <div className={styles.emptyState}>
          <p>No confirmed bookings yet for this hotel</p>
        </div>
      ) : (
        <div className={styles.barsWrap}>
          {data.map((room, i) => {
            const widthPct = (room.bookingsCount / maxCount) * 100;
            const color = barColor(i);
            const rank = RANK_LABELS[i];

            return (
              <div key={room.roomTypeName} className={styles.row}>
                <span className={styles.roomName} title={room.roomTypeName}>
                  {room.roomTypeName}
                </span>
                <div className={styles.trackWrap}>
                  <div
                    className={styles.track}
                    role="progressbar"
                    aria-valuenow={room.bookingsCount}
                    aria-valuemax={maxCount}
                  >
                    <div
                      className={styles.bar}
                      style={{
                        width: animated ? `${widthPct}%` : '0%',
                        background: color,
                        transition: animated ? `width 0.6s ease-out ${i * 80}ms` : 'none',
                      }}
                    />
                  </div>
                </div>
                <span className={styles.count}>{room.bookingsCount}</span>
                {rank && (
                  <span className={styles.rankBadge} style={{ color }}>
                    {rank}
                  </span>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default PopularRoomsChart;
