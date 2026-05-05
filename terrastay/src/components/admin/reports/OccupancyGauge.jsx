import { useState, useEffect } from 'react';
import { format, parse } from 'date-fns';
import { Building2 } from 'lucide-react';
import SectionError from '../../ui/SectionError';
import { parseApiError } from '../../../lib/parseApiError';
import styles from './OccupancyGauge.module.css';

const RADIUS = 78;
const CX = 100;
const CY = 105;
const TOTAL = Math.PI * RADIUS; // semi-circle arc length

const gaugeColor = (rate) => {
  if (rate < 30)  return { color: '#f87171', label: 'Low occupancy' };
  if (rate < 60)  return { color: '#fbbf24', label: 'Moderate occupancy' };
  if (rate < 85)  return { color: '#10b981', label: 'Good occupancy' };
  return           { color: '#3b82f6', label: 'Excellent!' };
};

const formatMonth = (monthStr) => {
  try {
    return format(parse(monthStr, 'yyyy-MM', new Date()), 'MMMM yyyy');
  } catch {
    return monthStr;
  }
};

const GaugeSvg = ({ rate }) => {
  const [animRate, setAnimRate] = useState(0);

  useEffect(() => {
    setAnimRate(0);
    const t = setTimeout(() => setAnimRate(rate), 80);
    return () => clearTimeout(t);
  }, [rate]);

  const { color, label } = gaugeColor(rate);
  const offset = TOTAL - (animRate / 100) * TOTAL;
  const arcPath = `M ${CX - RADIUS} ${CY} A ${RADIUS} ${RADIUS} 0 0 1 ${CX + RADIUS} ${CY}`;

  return (
    <div className={styles.svgWrap}>
      <svg viewBox="0 22 200 93" className={styles.svg} aria-label={`Occupancy gauge: ${rate}%`}>
        {/* Background track */}
        <path
          d={arcPath}
          fill="none"
          stroke="#f3f4f6"
          strokeWidth={14}
          strokeLinecap="round"
        />
        {/* Progress arc */}
        <path
          d={arcPath}
          fill="none"
          stroke={color}
          strokeWidth={14}
          strokeLinecap="round"
          strokeDasharray={TOTAL}
          strokeDashoffset={offset}
          style={{ transition: 'stroke-dashoffset 0.8s ease-out' }}
        />
        {/* Percentage label */}
        <text
          x={CX}
          y={CY - 16}
          textAnchor="middle"
          className={styles.gaugeValue}
          fill={color}
        >
          {rate.toFixed(1)}%
        </text>
        {/* Sub-label */}
        <text
          x={CX}
          y={CY - 2}
          textAnchor="middle"
          className={styles.gaugeLabel}
        >
          occupancy rate
        </text>
      </svg>
      <span className={styles.qualityLabel} style={{ color }}>{label}</span>
    </div>
  );
};

const Skeleton = () => (
  <div className={styles.card}>
    <div className={styles.cardHeader}>
      <div className="skeleton" style={{ height: 20, width: 200 }} />
    </div>
    <div className={styles.divider} />
    <div className={styles.skeletonGauge}>
      <div className="skeleton" style={{ width: 200, height: 110, borderRadius: '50% 50% 0 0 / 100%' }} />
    </div>
    <div className={styles.divider} />
    <div className="skeleton" style={{ height: 14, width: 120 }} />
  </div>
);

/**
 * Semi-circle SVG gauge showing hotel occupancy rate for a given month.
 * Animates from 0 → actual rate on mount (800ms ease-out).
 */
const OccupancyGauge = ({ data, isLoading, isError, error, onRetry }) => {
  if (isLoading) return <Skeleton />;

  if (isError) {
    return (
      <div className={styles.card}>
        <SectionError message={parseApiError(error).message} onRetry={onRetry} />
      </div>
    );
  }

  if (!data) return null;

  const isEmpty = data.totalRooms === 0;

  return (
    <div className={styles.card}>
      <div className={styles.cardHeader}>
        <div className={styles.titleRow}>
          <Building2 size={18} className={styles.icon} />
          <h2 className={styles.title}>Occupancy Rate</h2>
          <span className={styles.monthBadge}>{formatMonth(data.month)}</span>
        </div>
      </div>

      <div className={styles.divider} />

      {isEmpty ? (
        <div className={styles.emptyState}>
          <p>No booking data for this month</p>
        </div>
      ) : (
        <GaugeSvg rate={data.occupancyRate} />
      )}

      <div className={styles.divider} />

      <div className={styles.footer}>
        <span className={styles.footerItem}>
          <span className={styles.footerLabel}>Total rooms:</span>
          <strong>{data.totalRooms}</strong>
        </span>
        <span className={styles.footerItem}>
          <span className={styles.footerLabel}>Hotel:</span>
          <strong>ID #{data.hotelId}</strong>
        </span>
      </div>
    </div>
  );
};

export default OccupancyGauge;
