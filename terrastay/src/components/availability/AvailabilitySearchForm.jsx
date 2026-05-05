import { useRef, useEffect, useMemo, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format, addDays, differenceInDays, parseISO } from 'date-fns';
import { Search } from 'lucide-react';
import Input from '../ui/Input';
import Button from '../ui/Button';
import styles from './AvailabilitySearchForm.module.css';

const todayStr = format(new Date(), 'yyyy-MM-dd');
const tomorrowStr = format(addDays(new Date(), 1), 'yyyy-MM-dd');

const SORT_OPTIONS = [
  { value: 'basePrice,asc',  label: 'Price ↑' },
  { value: 'basePrice,desc', label: 'Price ↓' },
  { value: 'capacity,asc',   label: 'Capacity' },
  { value: 'name,asc',       label: 'Name' },
];

const schema = z.object({
  checkIn:       z.string().min(1, 'Check-in is required'),
  checkOut:      z.string().min(1, 'Check-out is required'),
  guests:        z.coerce.number().min(1, 'At least 1 guest required').max(20, 'Max 20 guests').optional().or(z.literal('')),
  q:             z.string().optional(),
  availableOnly: z.boolean().default(false),
  sort:          z.string().default('basePrice,asc'),
}).refine(
  (d) => !d.checkIn || d.checkIn >= todayStr,
  { message: 'Check-in must be today or in the future', path: ['checkIn'] }
).refine(
  (d) => !d.checkIn || !d.checkOut || d.checkOut > d.checkIn,
  { message: 'Check-out must be after check-in', path: ['checkOut'] }
);

/**
 * Availability search form with debounced auto-submit (400ms).
 * Also exposes an explicit [Search] button.
 *
 * @param {{ hotelId: number, onSearch: (params) => void, initialValues?: object }} props
 */
const AvailabilitySearchForm = ({ onSearch, initialValues = {} }) => {
  const debounceRef = useRef(null);

  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      checkIn:       initialValues.checkIn       || todayStr,
      checkOut:      initialValues.checkOut      || tomorrowStr,
      guests:        initialValues.guests        || '',
      q:             initialValues.q             || '',
      availableOnly: initialValues.availableOnly ?? false,
      sort:          initialValues.sort          || 'basePrice,asc',
    },
  });

  const { watch, setValue, register, handleSubmit, formState: { errors } } = form;
  const watched = watch();

  // Auto-advance checkOut when checkIn changes
  useEffect(() => {
    const checkIn = watched.checkIn;
    if (!checkIn) return;
    const minOut = format(addDays(parseISO(checkIn), 1), 'yyyy-MM-dd');
    if (!watched.checkOut || watched.checkOut <= checkIn) {
      setValue('checkOut', minOut, { shouldValidate: false });
    }
  }, [watched.checkIn]);

  // Nights counter
  const nights = useMemo(() => {
    if (!watched.checkIn || !watched.checkOut) return 0;
    return Math.max(0, differenceInDays(parseISO(watched.checkOut), parseISO(watched.checkIn)));
  }, [watched.checkIn, watched.checkOut]);

  // Debounced auto-submit
  const debouncedSubmit = useCallback((values) => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      if (!values.checkIn || !values.checkOut || values.checkOut <= values.checkIn) return;
      onSearch({
        checkIn:       values.checkIn,
        checkOut:      values.checkOut,
        guests:        values.guests || undefined,
        q:             values.q     || undefined,
        availableOnly: values.availableOnly,
        sort:          values.sort,
      });
    }, 400);
  }, [onSearch]);

  useEffect(() => {
    const subscription = form.watch((values) => debouncedSubmit(values));
    return () => {
      subscription.unsubscribe();
      clearTimeout(debounceRef.current);
    };
  }, [debouncedSubmit]);

  const onExplicitSubmit = (values) => {
    clearTimeout(debounceRef.current);
    onSearch({
      checkIn:       values.checkIn,
      checkOut:      values.checkOut,
      guests:        values.guests || undefined,
      q:             values.q     || undefined,
      availableOnly: values.availableOnly,
      sort:          values.sort,
    });
  };

  // Date strip text
  const strip = useMemo(() => {
    if (!watched.checkIn || !watched.checkOut) return null;
    try {
      const start = format(parseISO(watched.checkIn), 'MMM d');
      const end   = format(parseISO(watched.checkOut), 'MMM d, yyyy');
      const g     = Number(watched.guests);
      const gStr  = g > 0 ? ` · ${g} guest${g === 1 ? '' : 's'}` : '';
      return `${start} – ${end} · ${nights} night${nights === 1 ? '' : 's'}${gStr}`;
    } catch {
      return null;
    }
  }, [watched.checkIn, watched.checkOut, watched.guests, nights]);

  return (
    <div className={styles.wrap}>
      <form className={styles.form} onSubmit={handleSubmit(onExplicitSubmit)} noValidate>
        {/* Row 1: dates + nights counter */}
        <div className={styles.datesGroup}>
          <Input
            label="Check-in"
            type="date"
            min={todayStr}
            error={errors.checkIn?.message}
            {...register('checkIn')}
          />
          {nights > 0 && (
            <span className={styles.nightsCounter}>{nights} night{nights === 1 ? '' : 's'}</span>
          )}
          <Input
            label="Check-out"
            type="date"
            min={watched.checkIn ? format(addDays(parseISO(watched.checkIn), 1), 'yyyy-MM-dd') : tomorrowStr}
            error={errors.checkOut?.message}
            {...register('checkOut')}
          />
        </div>

        {/* Row 2: guests + search term */}
        <div className={styles.row}>
          <Input
            label="Guests"
            type="number"
            min="1"
            max="20"
            placeholder="Any"
            error={errors.guests?.message}
            {...register('guests')}
          />
          <Input
            label="Search room types"
            type="text"
            placeholder="e.g. Deluxe, Suite…"
            icon={Search}
            {...register('q')}
          />
        </div>

        {/* Row 3: toggles + sort + submit */}
        <div className={styles.controls}>
          <label className={styles.checkboxLabel}>
            <input type="checkbox" {...register('availableOnly')} />
            <span>Available rooms only</span>
          </label>

          <div className={styles.sortWrap}>
            <label className={styles.sortLabel}>Sort:</label>
            <select className={styles.sortSelect} {...register('sort')}>
              {SORT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>

          <Button type="submit" variant="primary" size="sm" icon={Search}>
            Search
          </Button>
        </div>
      </form>

      {strip && <div className={styles.strip}>{strip}</div>}
    </div>
  );
};

export default AvailabilitySearchForm;
