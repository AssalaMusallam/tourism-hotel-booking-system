import { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import usePriceBreakdown from '../../hooks/usePriceBreakdown';
import { CURRENCY_META } from '../../constants/currencies';
import { useCurrencyContext } from '../../context/CurrencyContext';
import { useRoomPriceInCurrency } from '../../hooks/useCurrency';
import styles from './PriceBreakdown.module.css';

const money = (value) =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const PriceBreakdown = ({ roomType, checkIn, checkOut }) => {
  const [open, setOpen] = useState(false);
  const { selectedCurrency, formatPrice } = useCurrencyContext();
  const breakdown = usePriceBreakdown(roomType, checkIn, checkOut);
  const roomTypeId = roomType?.roomTypeId || roomType?.id;
  const convertedQuery = useRoomPriceInCurrency(roomTypeId, checkIn, checkOut, selectedCurrency);
  const hasNightBreakdown = breakdown.breakdown.length > 1;
  const taxPercent = Math.round((breakdown.taxRate || 0.10) * 100);
  const converted = convertedQuery.data;
  const symbol = CURRENCY_META[selectedCurrency]?.symbol || selectedCurrency;

  return (
    <section className={styles.card} aria-label="Price breakdown">
      <h3 className={styles.title}>Price breakdown</h3>
      <div className={styles.line}>
        <span>
          {money(breakdown.pricePerNight)}/night x {breakdown.nights} night{breakdown.nights === 1 ? '' : 's'}
        </span>
        <span className={styles.amount}>{money(breakdown.subtotal)}</span>
      </div>
      <div className={styles.line}>
        <span>Taxes & fees ({taxPercent}%)</span>
        <span className={styles.amount}>{money(breakdown.taxes)}</span>
      </div>
      <div className={styles.divider} />
      <div className={styles.total}>
        <span>Total</span>
        <span>{money(breakdown.total)}</span>
      </div>

      <div className={styles.currencyBox}>
        {convertedQuery.isFetching ? (
          <div className="skeleton" style={{ height: 24, width: '72%' }} />
        ) : converted ? (
          <>
            <div className={styles.line}>
              <span>Total (USD)</span>
              <strong>{money(converted.originalTotalUSD)}</strong>
            </div>
            <div className={styles.line}>
              <span>Total ({converted.currency})</span>
              <strong className={styles.converted}>
                {new Intl.NumberFormat('en-US', {
                  style: 'currency',
                  currency: converted.currency,
                  maximumFractionDigits: 2,
                }).format(Number(converted.convertedTotal || 0))}
              </strong>
            </div>
            <p>1 USD = {Number(converted.exchangeRate || 1).toFixed(4)} {symbol} · Rates updated periodically</p>
          </>
        ) : (
          <p>{selectedCurrency === 'USD' ? 'Final charge processed in USD.' : `Estimated total: ${formatPrice(breakdown.total)}`}</p>
        )}
      </div>

      {hasNightBreakdown && (
        <>
          <button type="button" className={styles.toggle} onClick={() => setOpen((value) => !value)}>
            {open ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
            Per-night breakdown
          </button>
          {open && (
            <div className={styles.nights}>
              {breakdown.breakdown.map((night) => (
                <div key={night.date} className={styles.night}>
                  <span>
                    {night.date}
                    {night.rule && <span className={styles.rule}>({night.rule})</span>}
                  </span>
                  <strong>{money(night.rate)}</strong>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </section>
  );
};

export default PriceBreakdown;
