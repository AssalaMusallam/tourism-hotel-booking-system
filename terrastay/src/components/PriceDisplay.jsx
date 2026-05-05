import { useCurrencyContext } from '../context/CurrencyContext';
import styles from './PriceDisplay.module.css';

const usdFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 2,
});

const PriceDisplay = ({ usdAmount, showOriginal = false, size = 'md', suffix }) => {
  const { selectedCurrency, formatPrice } = useCurrencyContext();
  const amount = Number(usdAmount || 0);

  return (
    <span className={`${styles.price} ${styles[size]}`}>
      <span>
        {formatPrice(amount)}
        {suffix && <span className={styles.suffix}> {suffix}</span>}
      </span>
      {showOriginal && selectedCurrency !== 'USD' && (
        <small>(≈ {usdFormatter.format(amount)} USD)</small>
      )}
    </span>
  );
};

export default PriceDisplay;
