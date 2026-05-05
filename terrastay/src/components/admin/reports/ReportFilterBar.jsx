import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  format, startOfMonth, endOfMonth, subMonths,
  startOfYear, startOfQuarter, endOfQuarter, addMonths,
} from 'date-fns';
import { Play } from 'lucide-react';
import Input from '../../ui/Input';
import Button from '../../ui/Button';
import styles from './ReportFilterBar.module.css';

const todayStr = () => format(new Date(), 'yyyy-MM-dd');
const thisMonthStart = () => format(startOfMonth(new Date()), 'yyyy-MM-dd');
const thisMonthStr = () => format(new Date(), 'yyyy-MM');
const lastMonthStr = () => format(subMonths(new Date(), 1), 'yyyy-MM');

// ── Zod schemas ────────────────────────────────────────────────────────────────

const revenueSchema = z.object({
  from: z.string().min(1, 'From date is required'),
  to:   z.string().min(1, 'To date is required'),
}).refine((d) => d.to > d.from, {
  message: "'To' date must be after 'From' date",
  path: ['to'],
});

const occupancySchema = z.object({
  month: z.string()
    .min(1, 'Month is required')
    .regex(/^\d{4}-\d{2}$/, 'Use format YYYY-MM (e.g. 2026-03)'),
});

// ── Quick range helpers ────────────────────────────────────────────────────────

const quickRanges = () => {
  const now = new Date();
  const year = now.getFullYear();

  const q1Start = new Date(year, 0, 1);
  const q1End   = new Date(year, 2, 31);
  const q2Start = new Date(year, 3, 1);
  const q2End   = new Date(year, 5, 30);

  return {
    thisMonth:  { from: thisMonthStart(), to: todayStr() },
    lastMonth:  { from: format(startOfMonth(subMonths(now, 1)), 'yyyy-MM-dd'), to: format(endOfMonth(subMonths(now, 1)), 'yyyy-MM-dd') },
    q1:         { from: format(q1Start, 'yyyy-MM-dd'), to: format(q1End, 'yyyy-MM-dd') },
    q2:         { from: format(q2Start, 'yyyy-MM-dd'), to: format(q2End, 'yyyy-MM-dd') },
    ytd:        { from: format(startOfYear(now), 'yyyy-MM-dd'), to: todayStr() },
  };
};

// ── Revenue filter ─────────────────────────────────────────────────────────────

const RevenueFilter = ({ onRevenue }) => {
  const { register, handleSubmit, setValue, formState: { errors } } = useForm({
    resolver: zodResolver(revenueSchema),
    defaultValues: { from: thisMonthStart(), to: todayStr() },
  });

  const applyRange = (range) => {
    setValue('from', range.from);
    setValue('to', range.to);
  };

  const ranges = quickRanges();

  return (
    <div className={styles.filterSection}>
      <h3 className={styles.sectionTitle}>Revenue Report</h3>
      <form className={styles.form} onSubmit={handleSubmit(({ from, to }) => onRevenue(from, to))}>
        <div className={styles.dateRow}>
          <Input label="From" type="date" error={errors.from?.message} {...register('from')} />
          <Input label="To" type="date" error={errors.to?.message} {...register('to')} />
          <div className={styles.btnWrap}>
            <Button type="submit" variant="primary" size="sm" icon={Play}>Run</Button>
          </div>
        </div>
        <div className={styles.quickBtns}>
          {[
            { label: 'This Month', range: ranges.thisMonth },
            { label: 'Last Month', range: ranges.lastMonth },
            { label: 'Q1',         range: ranges.q1 },
            { label: 'Q2',         range: ranges.q2 },
            { label: 'YTD',        range: ranges.ytd },
          ].map(({ label, range }) => (
            <button key={label} type="button" className={styles.quickBtn} onClick={() => applyRange(range)}>
              {label}
            </button>
          ))}
        </div>
      </form>
    </div>
  );
};

// ── Occupancy filter ───────────────────────────────────────────────────────────

const OccupancyFilter = ({ onOccupancy }) => {
  const { register, handleSubmit, setValue, formState: { errors } } = useForm({
    resolver: zodResolver(occupancySchema),
    defaultValues: { month: thisMonthStr() },
  });

  return (
    <div className={styles.filterSection}>
      <h3 className={styles.sectionTitle}>Occupancy Report</h3>
      <form className={styles.form} onSubmit={handleSubmit(({ month }) => onOccupancy(month))}>
        <div className={styles.dateRow}>
          <Input
            label="Month (YYYY-MM)"
            type="month"
            error={errors.month?.message}
            {...register('month')}
          />
          <div className={styles.btnWrap}>
            <Button type="submit" variant="primary" size="sm" icon={Play}>Run</Button>
          </div>
        </div>
        <div className={styles.quickBtns}>
          {[
            { label: 'This Month', val: thisMonthStr() },
            { label: 'Last Month', val: lastMonthStr() },
          ].map(({ label, val }) => (
            <button key={label} type="button" className={styles.quickBtn} onClick={() => setValue('month', val)}>
              {label}
            </button>
          ))}
        </div>
      </form>
    </div>
  );
};

// ── Combined filter bar ───────────────────────────────────────────────────────

/**
 * Two-section filter bar: revenue (date range) + occupancy (month).
 * Quick range buttons fill form fields; user must click Run to submit.
 */
const ReportFilterBar = ({ onRevenue, onOccupancy }) => (
  <div className={styles.bar}>
    <RevenueFilter onRevenue={onRevenue} />
    <div className={styles.sectionDivider} />
    <OccupancyFilter onOccupancy={onOccupancy} />
  </div>
);

export default ReportFilterBar;
