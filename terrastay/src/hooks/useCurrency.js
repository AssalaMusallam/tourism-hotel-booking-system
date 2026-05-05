import { useQuery } from '@tanstack/react-query';
import { getRoomPriceInCurrency, getSupportedCurrencies } from '../api/currencyApi';

export const useSupportedCurrencies = () =>
  useQuery({
    queryKey: ['currencies'],
    queryFn: getSupportedCurrencies,
    staleTime: Infinity,
    gcTime: Infinity,
  });

export const useRoomPriceInCurrency = (roomTypeId, checkIn, checkOut, currency) =>
  useQuery({
    queryKey: ['roomPrice', roomTypeId, checkIn, checkOut, currency],
    queryFn: () => getRoomPriceInCurrency({ roomTypeId, checkIn, checkOut, currency }),
    enabled: Boolean(roomTypeId && checkIn && checkOut && currency),
    staleTime: 5 * 60 * 1000,
  });
