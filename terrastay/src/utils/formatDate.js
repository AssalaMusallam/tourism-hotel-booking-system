import { format, parseISO, differenceInDays } from 'date-fns';

export const formatDate = (date, fmt = 'MMM d, yyyy') => {
  if (!date) return '';
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, fmt);
};

export const formatDateShort = (date) => formatDate(date, 'MMM d');

export const getNights = (checkIn, checkOut) => {
  if (!checkIn || !checkOut) return 0;
  const a = typeof checkIn === 'string' ? parseISO(checkIn) : checkIn;
  const b = typeof checkOut === 'string' ? parseISO(checkOut) : checkOut;
  return Math.max(0, differenceInDays(b, a));
};

export const toInputDate = (date) => {
  if (!date) return '';
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, 'yyyy-MM-dd');
};
