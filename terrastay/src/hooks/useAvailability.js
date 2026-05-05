import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';

export const availabilityKeys = {
  all: ['availability'],
  hotel: (hotelId, checkIn, checkOut, guests, params = {}) => [
    'availability',
    'hotel',
    hotelId,
    checkIn,
    checkOut,
    guests,
    params,
  ],
};

export const fetchAvailability = async (hotelId, checkIn, checkOut, guests, params = {}) => {
  const response = await api.get(`/api/v1/hotels/${hotelId}/availability`, {
    params: {
      checkIn,
      checkOut,
      guests,
      availableOnly: true,
      page: 0,
      size: 50,
      sort: 'basePrice,asc',
      ...params,
    },
  });
  return response.data;
};

const useAvailability = (hotelId, checkIn, checkOut, guests, params = {}) =>
  useQuery({
    queryKey: availabilityKeys.hotel(hotelId, checkIn, checkOut, guests, params),
    queryFn: () => fetchAvailability(hotelId, checkIn, checkOut, guests, params),
    enabled: Boolean(hotelId && checkIn && checkOut),
    staleTime: 5 * 60 * 1000,
    placeholderData: (previous) => previous,
  });

export default useAvailability;
