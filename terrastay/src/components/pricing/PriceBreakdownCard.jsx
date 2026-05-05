import { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import { format, parseISO } from 'date-fns';
import { useCurrencyContext } from '../../context/CurrencyContext';
import styles from './PriceBreakdownCard.module.css';

const money = (v) =>
  Number(v || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const fmtDate = (str) => {
  try { return format(parseISO(str), 'EEE MMM d'); }
  catch { return str; }
};

const fmtDay = (str) => {
  try { return format(parseISO(str), 'EEE'); }
  catch { return ''; }
};

const seasonLabel = (m) => {
  if (!m || m === 1) return null;
  const pct = Math.round((m - 1) * 100);
  return pct > 0 ? `+${pct}%` : `${pct}%`;
};

const NightRow = ({ night }) => {
  const hasSeason = night.seasonMultiplier !== 1;
  const isWe = night.isWeekend;
  const rowClass = [
    styles.nightRow,
    isWe && hasSeason ? styles.rowBoth : isWe ? styles.rowWeekend : hasSeason ? styles.rowSeason : '',
  ].filter(Boolean).join(' ');

  return (
    <tr className={rowClass}>
      <td>{fmtDate(night.date)}</td>
      <td>{fmtDay(night.date)}</td>
      <td>{money(night.baseRate)}</td>
      <td>
        {hasSeason
          ? <span className={styles.seasonTag}>⚡ {seasonLabel(night.seasonMultiplier)}</span>
          : <span className={styles.standardTag}>Standard</span>}
      </td>
      <td>{night.appliedRuleName || '—'}</td>
      <td>
        {isWe ? <span className={styles.weekendTag}>🌙 +25%</span> : '—'}
      </td>
      <td className={styles.nightTotal}>{money(night.nightTotal)}</td>
    </tr>
  );
};

const PriceBreakdownCard = ({ breakdown, currency }) => {
  const [open, setOpen] = useState(false);
  const { formatPrice, selectedCurrency } = useCurrencyContext();

  if (!breakdown) return null;

  const taxPct = Math.round((breakdown.taxRate || 0.16) * 100);
  const displayCurrency = currency || selectedCurrency;

  return (
    <section className={styles.card} aria-label="Price breakdown">
      <div className={styles.headerLine}>
        <span className={styles.nightsSummary}>
          📅 {breakdown.nights} Night{breakdown.nights === 1 ? '' : 's'} · Base rate {money(breakdown.basePrice)}/night
        </span>
      </div>

      <div className={styles.divider} />

      <div className={styles.line}>
        <span>Subtotal</span>
        <strong>{money(breakdown.subtotal)}</strong>
      </div>
      <div className={styles.line}>
        <span>Tax ({taxPct}%)</span>
        <strong>+ {money(breakdown.taxAmount)}</strong>
      </div>

      <div className={styles.divider} />

      <div className={styles.total}>
        <span>TOTAL</span>
        <strong>{money(breakdown.totalPrice)}</strong>
      </div>

      {displayCurrency !== 'USD' && (
        <div className={styles.converted}>
          [in {displayCurrency}: {formatPrice(breakdown.totalPrice)}]
        </div>
      )}

      <button
        type="button"
        className={styles.toggle}
        onClick={() => setOpen((v) => !v)}
      >
        {open ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
        {open ? 'Hide' : 'View'} night-by-night breakdown
      </button>

      {open && breakdown.breakdown?.length > 0 && (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Date</th>
                <th>Day</th>
                <th>Base</th>
                <th>Season</th>
                <th>Rule Name</th>
                <th>Weekend</th>
                <th>Night Total</th>
              </tr>
            </thead>
            <tbody>
              {breakdown.breakdown.map((night) => (
                <NightRow key={night.date} night={night} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

export default PriceBreakdownCard;
