import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';

export const availabilityKeys = {
  all: ['availability'],
  hotel: (hotelId, checkIn, checkOut, guests, params = {}) => [
    'availability', 'hotel', hotelId, checkIn, checkOut, guests, params,
  ],
  room: (roomTypeId, checkIn, checkOut, guests) => [
    'availability', 'room', roomTypeId, checkIn, checkOut, guests,
  ],
};

// ── API functions ─────────────────────────────────────────────────────────────

export const fetchAvailability = async (hotelId, checkIn, checkOut, guests, params = {}) => {
  const response = await api.get(`/api/v1/hotels/${hotelId}/availability`, {
    params: {
      checkIn,
      checkOut,
      guests,
      availableOnly: false,
      page: 0,
      size: 50,
      sort: 'basePrice,asc',
      ...params,
    },
  });
  return response.data;
};

export const fetchRoomAvailability = async (roomTypeId, checkIn, checkOut, guests) => {
  const response = await api.get('/api/v1/availability', {
    params: {
      roomTypeId,
      checkIn,
      checkOut,
      ...(guests ? { guests } : {}),
    },
  });
  return response.data;
};

export const fetchHotelAvailabilityPaged = async (hotelId, params = {}) => {
  const cleaned = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== '' && v !== null)
  );
  const response = await api.get(`/api/v1/hotels/${hotelId}/availability`, { params: cleaned });
  return response.data;
};

// ── Hooks ─────────────────────────────────────────────────────────────────────

/**
 * Fetch availability for a single room type.
 * Returns AvailabilityResponseDto (includes priceBreakdown).
 */
export const useRoomAvailability = (roomTypeId, checkIn, checkOut, guests, enabled = true) =>
  useQuery({
    queryKey: availabilityKeys.room(roomTypeId, checkIn, checkOut, guests),
    queryFn: () => fetchRoomAvailability(roomTypeId, checkIn, checkOut, guests),
    enabled: !!(roomTypeId && checkIn && checkOut && enabled),
    staleTime: 3 * 60 * 1000,
  });

/**
 * Fetch paginated availability for all room types in a hotel.
 * Returns PagedResponse<AvailabilitySummaryDto>.
 *
 * @param {number|string} hotelId
 * @param {{ checkIn, checkOut, guests?, q?, availableOnly?, page?, size?, sort? }} params
 */
export const useHotelAvailability = (
  hotelId,
  { checkIn, checkOut, guests, q, availableOnly, page = 0, size = 10, sort } = {}
) =>
  useQuery({
    queryKey: [
      'availability', 'hotel', hotelId,
      checkIn, checkOut, guests, q, availableOnly, page, size, sort,
    ],
    queryFn: () =>
      fetchHotelAvailabilityPaged(hotelId, {
        checkIn,
        checkOut,
        guests,
        q,
        availableOnly,
        page,
        size,
        sort,
      }),
    enabled: !!(hotelId && checkIn && checkOut),
    staleTime: 3 * 60 * 1000,
    placeholderData: (previous) => previous,
  });

// ── Legacy hook (used by existing AvailabilityPage) ───────────────────────────

const useAvailability = (hotelId, checkIn, checkOut, guests, params = {}) =>
  useQuery({
    queryKey: availabilityKeys.hotel(hotelId, checkIn, checkOut, guests, params),
    queryFn: () => fetchAvailability(hotelId, checkIn, checkOut, guests, params),
    enabled: Boolean(hotelId && checkIn && checkOut),
    staleTime: 5 * 60 * 1000,
    placeholderData: (previous) => previous,
  });

export default useAvailability;
