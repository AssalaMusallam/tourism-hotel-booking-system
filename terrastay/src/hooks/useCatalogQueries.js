import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  searchHotels, getHotelById, getCities, adminSearchHotels,
  adminGetHotelById, createHotel, updateHotel, deleteHotel, getHotelImages,
  uploadHotelImages, deleteHotelImage,
} from '../api/hotelsApi';
import {
  getRoomTypesByHotel, createRoomType, updateRoomType,
  deleteRoomType, changeRoomTypeStatus, replaceRoomAmenities,
} from '../api/roomsApi';
import {
  getAmenities, getMinimalAmenities, createAmenity,
  updateAmenity, deleteAmenity, activateAmenity, deactivateAmenity,
} from '../api/amenitiesApi';

// ─── Query Keys ───────────────────────────────────────────────────────────────
export const hotelKeys = {
  all:         ['hotels'],
  search:      (p) => ['hotels', 'search', p],
  detail:      (id) => ['hotels', 'detail', id],
  cities:      ['hotels', 'cities'],
  adminAll:    ['admin', 'hotels'],
  adminSearch: (p) => ['admin', 'hotels', 'search', p],
  images:      (id) => ['admin', 'hotels', 'images', id],
};

export const roomKeys = {
  byHotel:  (hotelId, p) => ['rooms', 'hotel', hotelId, p],
  detail:   (id) => ['rooms', 'detail', id],
  minimal:  (hotelId, p) => ['rooms', 'minimal', hotelId, p],
};

export const amenityKeys = {
  list:    (p) => ['amenities', 'list', p],
  detail:  (id) => ['amenities', 'detail', id],
  minimal: (p) => ['amenities', 'minimal', p],
};

// ─── Hotel Queries ────────────────────────────────────────────────────────────
export const useHotels = (params) =>
  useQuery({
    queryKey: hotelKeys.search(params),
    queryFn:  () => searchHotels(params),
    staleTime: 5 * 60 * 1000,
    placeholderData: (prev) => prev,
  });

export const useHotel = (id) =>
  useQuery({
    queryKey: hotelKeys.detail(id),
    queryFn:  () => getHotelById(id),
    staleTime: 10 * 60 * 1000,
    enabled: !!id,
  });

export const useCities = () =>
  useQuery({
    queryKey: hotelKeys.cities,
    queryFn:  getCities,
    staleTime: 30 * 60 * 1000,
  });

export const useAdminHotels = (params) =>
  useQuery({
    queryKey: hotelKeys.adminSearch(params),
    queryFn:  () => adminSearchHotels(params),
    staleTime: 2 * 60 * 1000,
    placeholderData: (prev) => prev,
  });

export const useAdminHotel = (id) =>
  useQuery({
    queryKey: ['admin', 'hotels', 'detail', id],
    queryFn:  () => adminGetHotelById(id),
    enabled: !!id,
  });

export const useHotelImages = (hotelId) =>
  useQuery({
    queryKey: hotelKeys.images(hotelId),
    queryFn:  () => getHotelImages(hotelId),
    enabled:  !!hotelId,
  });

// ─── Hotel Mutations ──────────────────────────────────────────────────────────
export const useCreateHotel = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: createHotel,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: hotelKeys.all });
      qc.invalidateQueries({ queryKey: ['admin', 'hotels'] });
    },
  });
};

export const useUpdateHotel = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => updateHotel(id, data),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: hotelKeys.detail(id) });
      qc.invalidateQueries({ queryKey: hotelKeys.all });
      qc.invalidateQueries({ queryKey: ['admin', 'hotels'] });
    },
  });
};

export const useDeleteHotel = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deleteHotel,
    onMutate: async (id) => {
      await qc.cancelQueries({ queryKey: hotelKeys.adminSearch({}) });
      const snapshots = qc.getQueriesData({ queryKey: ['admin', 'hotels'] });
      qc.setQueriesData({ queryKey: ['admin', 'hotels'] }, (old) => {
        if (!old?.content) return old;
        return { ...old, content: old.content.filter((h) => h.id !== id) };
      });
      return { snapshots };
    },
    onError: (_, __, ctx) => {
      ctx?.snapshots?.forEach(([key, data]) => qc.setQueryData(key, data));
    },
    onSettled: () => {
      qc.invalidateQueries({ queryKey: ['admin', 'hotels'] });
      qc.invalidateQueries({ queryKey: hotelKeys.all });
    },
  });
};

export const useUploadHotelImages = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ hotelId, files }) => uploadHotelImages(hotelId, files),
    onSuccess: (_, { hotelId }) => {
      qc.invalidateQueries({ queryKey: hotelKeys.images(hotelId) });
      qc.invalidateQueries({ queryKey: hotelKeys.detail(hotelId) });
    },
  });
};

export const useDeleteHotelImage = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ hotelId, imageId }) => deleteHotelImage(hotelId, imageId),
    onSuccess: (_, { hotelId }) => {
      qc.invalidateQueries({ queryKey: hotelKeys.images(hotelId) });
      qc.invalidateQueries({ queryKey: hotelKeys.detail(hotelId) });
    },
  });
};

// ─── Room Queries ─────────────────────────────────────────────────────────────
export const useRoomsByHotel = (hotelId, params) =>
  useQuery({
    queryKey: roomKeys.byHotel(hotelId, params),
    queryFn:  () => getRoomTypesByHotel(hotelId, params),
    enabled:  !!hotelId,
    staleTime: 2 * 60 * 1000,
    placeholderData: (prev) => prev,
  });

// ─── Room Mutations ───────────────────────────────────────────────────────────
export const useCreateRoom = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ hotelId, data }) => createRoomType(hotelId, data),
    onSuccess: (_, { hotelId }) => {
      qc.invalidateQueries({ queryKey: ['rooms', 'hotel', hotelId] });
    },
  });
};

export const useUpdateRoom = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ hotelId, id, data }) => updateRoomType(hotelId, id, data),
    onSuccess: (_, { hotelId }) => {
      qc.invalidateQueries({ queryKey: ['rooms', 'hotel', hotelId] });
    },
  });
};

export const useDeleteRoom = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ hotelId, id }) => deleteRoomType(hotelId, id),
    onSuccess: (_, { hotelId }) => {
      qc.invalidateQueries({ queryKey: ['rooms', 'hotel', hotelId] });
    },
  });
};

export const useChangeRoomStatus = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ hotelId, id, status }) => changeRoomTypeStatus(hotelId, id, status),
    onSuccess: (_, { hotelId }) => {
      qc.invalidateQueries({ queryKey: ['rooms', 'hotel', hotelId] });
    },
  });
};

export const useReplaceRoomAmenities = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ roomTypeId, hotelId, amenityIds }) => replaceRoomAmenities(roomTypeId, amenityIds),
    onSuccess: (_, { hotelId }) => {
      qc.invalidateQueries({ queryKey: ['rooms', 'hotel', hotelId] });
    },
  });
};

// ─── Amenity Queries ──────────────────────────────────────────────────────────
export const useAmenities = (params) =>
  useQuery({
    queryKey: amenityKeys.list(params),
    queryFn:  () => getAmenities(params),
    staleTime: 2 * 60 * 1000,
    placeholderData: (prev) => prev,
  });

export const useMinimalAmenities = (params) =>
  useQuery({
    queryKey: amenityKeys.minimal(params),
    queryFn:  () => getMinimalAmenities(params),
    staleTime: 30 * 60 * 1000,
  });

// ─── Amenity Mutations (ADMIN only) ──────────────────────────────────────────
export const useCreateAmenity = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: createAmenity,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['amenities'] }),
  });
};

export const useUpdateAmenity = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => updateAmenity(id, data),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['amenities'] });
    },
  });
};

export const useDeleteAmenity = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deleteAmenity,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['amenities'] }),
  });
};

export const useToggleAmenityStatus = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, active }) => active ? activateAmenity(id) : deactivateAmenity(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['amenities'] }),
  });
};
