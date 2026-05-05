import { addDays, differenceInDays, format, parseISO } from 'date-fns';

const TAX_RATE = 0.10;

const toNumber = (value) => {
  const number = Number(value);
  return Number.isFinite(number) ? number : 0;
};

const normalizeDate = (value) => {
  if (!value) return null;
  return value instanceof Date ? value : parseISO(value);
};

export const buildPriceBreakdown = (roomType, checkIn, checkOut, taxRate = TAX_RATE) => {
  const start = normalizeDate(checkIn);
  const end = normalizeDate(checkOut);
  const nights = start && end ? Math.max(0, differenceInDays(end, start)) : 0;
  const pricePerNight = toNumber(
    roomType?.pricePerNight ??
    roomType?.basePrice ??
    roomType?.priceBreakdown?.basePrice
  );

  const backend = roomType?.priceBreakdown;
  if (backend) {
    const subtotal = toNumber(backend.subtotal);
    const taxes = toNumber(backend.taxAmount);
    return {
      nights: Number(backend.nights ?? nights),
      pricePerNight: toNumber(backend.basePrice ?? pricePerNight),
      taxRate: toNumber(backend.taxRate ?? taxRate),
      subtotal,
      taxes,
      total: toNumber(backend.totalPrice ?? subtotal + taxes),
      breakdown: (backend.breakdown || []).map((night, index) => ({
        date: night.date || (start ? format(addDays(start, index), 'yyyy-MM-dd') : ''),
        rate: toNumber(night.nightTotal ?? night.baseRate ?? pricePerNight),
        rule: night.appliedRuleName,
        isWeekend: Boolean(night.weekend),
      })),
    };
  }

  const subtotal = pricePerNight * nights;
  const taxes = subtotal * taxRate;
  const breakdown = Array.from({ length: nights }, (_, index) => ({
    date: start ? format(addDays(start, index), 'yyyy-MM-dd') : '',
    rate: pricePerNight,
    rule: null,
    isWeekend: false,
  }));

  return {
    nights,
    pricePerNight,
    taxRate,
    subtotal,
    taxes,
    total: subtotal + taxes,
    breakdown,
  };
};

const usePriceBreakdown = (roomType, checkIn, checkOut) =>
  buildPriceBreakdown(roomType, checkIn, checkOut);

export default usePriceBreakdown;
