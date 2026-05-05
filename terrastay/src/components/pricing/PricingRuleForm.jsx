import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format } from 'date-fns';
import Input from '../ui/Input';
import Button from '../ui/Button';
import styles from './PricingRuleForm.module.css';

const todayStr = format(new Date(), 'yyyy-MM-dd');

const schema = z.object({
  name: z.string().trim().min(2, 'Name must be 2–100 characters').max(100, 'Name must be 2–100 characters'),
  description: z.string().max(500, 'Max 500 characters').optional().or(z.literal('')),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().min(1, 'End date is required'),
  priceMultiplier: z.coerce
    .number({ invalid_type_error: 'Enter a valid number' })
    .min(0.1, 'Multiplier must be at least 0.1')
    .max(10.0, 'Multiplier cannot exceed 10.0'),
  active: z.boolean().default(true),
}).refine(
  (d) => d.startDate >= todayStr,
  { message: 'Start date must be today or in the future', path: ['startDate'] }
).refine(
  (d) => d.endDate > todayStr,
  { message: 'End date must be in the future', path: ['endDate'] }
).refine(
  (d) => d.endDate > d.startDate,
  { message: 'End date must be strictly after start date', path: ['endDate'] }
);

const multiplierHint = (m) => {
  const n = Number(m);
  if (!n || n === 1) return 'Standard pricing — no change';
  const pct = Math.round(Math.abs(n - 1) * 100);
  if (n >= 2) return 'Double price — high demand period';
  if (n > 1) return `${pct}% price increase (e.g. ${n >= 1.5 ? 'peak summer' : 'holiday surcharge'})`;
  return `${pct}% discount (e.g. off-season deal)`;
};

const MONTHS = ['J', 'F', 'M', 'A', 'M', 'J', 'J', 'A', 'S', 'O', 'N', 'D'];
const MONTH_NAMES = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

const DateRangePreview = ({ startDate, endDate }) => {
  if (!startDate || !endDate) return null;
  const year = new Date().getFullYear();
  return (
    <div className={styles.calendarPreview}>
      {MONTHS.map((label, i) => {
        const monthStart = `${year}-${String(i + 1).padStart(2, '0')}-01`;
        const nextMonth = i < 11 ? `${year}-${String(i + 2).padStart(2, '0')}-01` : `${year + 1}-01-01`;
        const inRange = monthStart < endDate && nextMonth > startDate;
        return (
          <div
            key={i}
            className={`${styles.monthBlock} ${inRange ? styles.monthHighlight : ''}`}
            title={MONTH_NAMES[i]}
          >
            {label}
          </div>
        );
      })}
    </div>
  );
};

const PricingRuleForm = ({ rule, onSubmit, loading, overlapError }) => {
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: rule?.name || '',
      description: rule?.description || '',
      startDate: rule?.startDate || '',
      endDate: rule?.endDate || '',
      priceMultiplier: rule?.priceMultiplier ?? 1.0,
      active: rule?.active ?? true,
    },
  });

  const watched = form.watch();
  const m = Number(watched.priceMultiplier || 1);
  const multiplierClass = m > 1 ? styles.increase : m < 1 ? styles.discount : styles.neutral;

  useEffect(() => {
    if (rule) form.reset({
      name: rule.name || '',
      description: rule.description || '',
      startDate: rule.startDate || '',
      endDate: rule.endDate || '',
      priceMultiplier: rule.priceMultiplier ?? 1.0,
      active: rule.active ?? true,
    });
  }, [rule, form]);

  useEffect(() => {
    if (overlapError) {
      form.setError('endDate', { type: 'server', message: overlapError });
    }
  }, [overlapError, form]);

  const submit = (values) => {
    onSubmit({
      ...values,
      priceMultiplier: Number(values.priceMultiplier),
      description: values.description?.trim() || null,
    });
  };

  return (
    <form className={styles.form} onSubmit={form.handleSubmit(submit)}>
      <Input
        label="Rule name *"
        placeholder="e.g. Summer Peak 2026"
        error={form.formState.errors.name?.message}
        {...form.register('name')}
      />

      <div className={styles.field}>
        <label className={styles.label}>Description</label>
        <textarea
          className={styles.textarea}
          rows={3}
          maxLength={500}
          placeholder="Optional description..."
          {...form.register('description')}
        />
        {form.formState.errors.description && (
          <span className={styles.err}>{form.formState.errors.description.message}</span>
        )}
      </div>

      <div className={styles.row}>
        <Input
          label="Start date *"
          type="date"
          min={todayStr}
          error={form.formState.errors.startDate?.message}
          {...form.register('startDate')}
        />
        <Input
          label="End date *"
          type="date"
          min={todayStr}
          error={form.formState.errors.endDate?.message}
          {...form.register('endDate')}
        />
      </div>

      <DateRangePreview startDate={watched.startDate} endDate={watched.endDate} />

      {overlapError && (
        <div className={styles.overlapError}>
          ⚠️ {overlapError}
        </div>
      )}

      <div className={styles.field}>
        <label className={styles.label}>Price multiplier *</label>
        <div className={styles.multiplierWrap}>
          <input
            type="number"
            step="0.05"
            min="0.1"
            max="10.0"
            className={`${styles.multiplierInput} ${multiplierClass}`}
            {...form.register('priceMultiplier')}
          />
          <span className={`${styles.multiplierPreview} ${multiplierClass}`}>
            ×{isNaN(m) ? '1.00' : m.toFixed(2)} = {isNaN(m) ? 'no change' : (
              m === 1 ? 'no change' : m > 1 ? `prices +${Math.round((m - 1) * 100)}%` : `prices -${Math.round((1 - m) * 100)}%`
            )}
          </span>
        </div>
        {form.formState.errors.priceMultiplier && (
          <span className={styles.err}>{form.formState.errors.priceMultiplier.message}</span>
        )}
        <span className={styles.hint}>{multiplierHint(watched.priceMultiplier)}</span>
      </div>

      <div className={styles.toggleRow}>
        <label className={styles.toggleLabel}>Active</label>
        <label className={styles.switch}>
          <input type="checkbox" {...form.register('active')} />
          <span className={styles.slider} />
        </label>
      </div>

      <Button type="submit" variant="primary" fullWidth loading={loading}>
        {rule ? 'Update Rule' : 'Create Rule'}
      </Button>
    </form>
  );
};

export default PricingRuleForm;
