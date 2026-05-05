import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  cancelWaitingListEntry,
  getAdminWaitingList,
  getMyWaitingList,
  getWaitingListCount,
  joinWaitingList,
  waitingListKeys,
} from '../api/waitingListApi';
import useAuth from './useAuth';

export const useMyWaitingList = (page = 0) => {
  const { isAuthenticated, isGuest } = useAuth();
  return useQuery({
    queryKey: waitingListKeys.mine(page),
    queryFn: () => getMyWaitingList(page, 10),
    enabled: Boolean(isAuthenticated && isGuest),
    staleTime: 0,
    refetchInterval: 2 * 60 * 1000,
  });
};

export const useJoinWaitingList = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: joinWaitingList,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waitingListKeys.all });
    },
  });
};

export const useCancelWaitingListEntry = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: cancelWaitingListEntry,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waitingListKeys.all });
    },
  });
};

export const useAdminWaitingList = (roomTypeId, page = 0) => {
  const { isManager } = useAuth();
  return useQuery({
    queryKey: waitingListKeys.admin(roomTypeId, page),
    queryFn: () => getAdminWaitingList(roomTypeId, page, 20),
    enabled: Boolean(isManager && roomTypeId),
    staleTime: 0,
  });
};

export const useWaitingListCount = (roomTypeId, checkIn, checkOut) => {
  const { isManager } = useAuth();
  return useQuery({
    queryKey: waitingListKeys.count(roomTypeId, checkIn, checkOut),
    queryFn: () => getWaitingListCount(roomTypeId, checkIn, checkOut),
    enabled: Boolean(isManager && roomTypeId && checkIn && checkOut),
    staleTime: 60 * 1000,
  });
};
