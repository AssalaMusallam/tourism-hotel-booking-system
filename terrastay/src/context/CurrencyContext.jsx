import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import { CURRENCY_META, SUPPORTED_CURRENCY_CODES } from '../constants/currencies';
import { useSupportedCurrencies } from '../hooks/useCurrency';

const STORAGE_KEY = 'preferred_currency';
const CurrencyContext = createContext(null);

const usdRate = { id: 0, fromCurrency: 'USD', toCurrency: 'USD', rate: 1, updatedAt: null };

export const CurrencyProvider = ({ children }) => {
  const [selectedCurrency, setSelectedCurrencyState] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    return SUPPORTED_CURRENCY_CODES.includes(saved) ? saved : 'USD';
  });
  const ratesQuery = useSupportedCurrencies();

  const supportedCurrencies = useMemo(() => {
    const fromApi = Array.isArray(ratesQuery.data) ? ratesQuery.data : [];
    const merged = [usdRate, ...fromApi].filter((rate, index, list) =>
      list.findIndex((item) => item.toCurrency === rate.toCurrency) === index
    );
    return SUPPORTED_CURRENCY_CODES
      .map((code) => merged.find((rate) => rate.toCurrency === code) || { ...usdRate, toCurrency: code, rate: code === 'USD' ? 1 : null })
      .filter((rate) => rate.rate != null);
  }, [ratesQuery.data]);

  useEffect(() => {
    if (selectedCurrency === 'USD') return;
    const exists = supportedCurrencies.some((rate) => rate.toCurrency === selectedCurrency);
    if (!exists && !ratesQuery.isLoading) {
      toast.error('This currency is not supported. Showing prices in USD.');
      setSelectedCurrencyState('USD');
      localStorage.setItem(STORAGE_KEY, 'USD');
    }
  }, [ratesQuery.isLoading, selectedCurrency, supportedCurrencies]);

  const setSelectedCurrency = (currency) => {
    const next = SUPPORTED_CURRENCY_CODES.includes(currency) ? currency : 'USD';
    setSelectedCurrencyState(next);
    localStorage.setItem(STORAGE_KEY, next);
  };

  const getRate = (toCurrency) => {
    if (toCurrency === 'USD') return 1;
    const rate = supportedCurrencies.find((item) => item.toCurrency === toCurrency);
    return Number(rate?.rate || 1);
  };

  const formatPrice = (usdAmount, options = {}) => {
    const currency = options.currency || selectedCurrency;
    const amount = Number(usdAmount || 0) * getRate(currency);
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency,
      maximumFractionDigits: 2,
    }).format(amount);
  };

  const value = useMemo(() => ({
    selectedCurrency,
    setSelectedCurrency,
    supportedCurrencies,
    getRate,
    formatPrice,
    meta: CURRENCY_META[selectedCurrency] || CURRENCY_META.USD,
    isLoading: ratesQuery.isLoading,
  }), [selectedCurrency, supportedCurrencies, ratesQuery.isLoading]);

  return <CurrencyContext.Provider value={value}>{children}</CurrencyContext.Provider>;
};

export const useCurrencyContext = () => {
  const value = useContext(CurrencyContext);
  if (!value) throw new Error('useCurrencyContext must be used within CurrencyProvider');
  return value;
};

export default CurrencyContext;
