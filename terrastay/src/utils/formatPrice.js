export const formatPrice = (amount, currency = 'USD') => {
  const locale = document.documentElement.lang === 'ar' ? 'ar-EG-u-nu-arab' : 'en-US';
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
};

export const formatPriceILS = (amount) => formatPrice(amount, 'ILS');
