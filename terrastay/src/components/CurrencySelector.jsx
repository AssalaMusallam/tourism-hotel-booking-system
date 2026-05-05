import { useState } from 'react';
import { Check, ChevronDown } from 'lucide-react';
import { CURRENCY_META } from '../constants/currencies';
import { useCurrencyContext } from '../context/CurrencyContext';
import styles from './CurrencySelector.module.css';

const CurrencySelector = ({ mobile = false }) => {
  const [open, setOpen] = useState(false);
  const { selectedCurrency, setSelectedCurrency, supportedCurrencies } = useCurrencyContext();
  const selectedMeta = CURRENCY_META[selectedCurrency] || CURRENCY_META.USD;

  const choose = (currency) => {
    setSelectedCurrency(currency);
    setOpen(false);
  };

  return (
    <div className={`${styles.wrap} ${mobile ? styles.mobile : ''}`}>
      <button type="button" className={styles.trigger} onClick={() => setOpen((value) => !value)}>
        <span>{selectedMeta.flag}</span>
        <strong>{selectedCurrency}</strong>
        <ChevronDown size={14} />
      </button>
      {open && (
        <div className={styles.menu}>
          {supportedCurrencies.map((rate) => {
            const code = rate.toCurrency;
            const meta = CURRENCY_META[code] || { flag: '💱', name: code };
            return (
              <button key={code} type="button" className={styles.option} onClick={() => choose(code)}>
                <span>{meta.flag}</span>
                <span>{code}</span>
                <small>{meta.name}</small>
                {selectedCurrency === code && <Check size={15} />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default CurrencySelector;
