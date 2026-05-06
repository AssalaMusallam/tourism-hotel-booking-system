import { useQuery } from '@tanstack/react-query';
import { getMe } from '../api/usersApi';
import useAuthStore from '../store/authStore';

export function useManagerHotel() {
  const user = useAuthStore((state) => state.user);

  const hotelIdFromStore =
    user?.hotelId ||
    user?.managedHotelId ||
    user?.managedHotels?.[0]?.id ||
    user?.hotels?.[0]?.id ||
    null;

  const { data: meData, isLoading } = useQuery({
    queryKey: ['manager-me-hotel'],
    queryFn: async () => {
      const data = await getMe();
      console.log('[useManagerHotel] /api/users/me response:', JSON.stringify(data, null, 2));
      return data;
    },
    enabled: !hotelIdFromStore,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });

  const hotelId =
    hotelIdFromStore ||
    meData?.hotelId ||
    meData?.managedHotelId ||
    meData?.managedHotels?.[0]?.id ||
    meData?.hotels?.[0]?.id ||
    null;

  return { hotelId, isLoadingHotelId: isLoading && !hotelIdFromStore };
}

export default useManagerHotel;
