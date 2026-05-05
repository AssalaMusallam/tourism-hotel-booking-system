import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getNotificationById,
  getNotificationStats,
  getNotifications,
  getNotificationsByEmail,
  getNotificationsByReference,
  getNotificationsByStatus,
  retryNotification,
  sendCustomNotification,
  sendNotification,
} from '../api/notifications';

export const notificationKeys = {
  all: ['notifications'],
  list: (filters) => ['notifications', 'list', filters],
  detail: (id) => ['notifications', 'detail', id],
  stats: ['notifications', 'stats'],
};

export const useNotificationStats = () =>
  useQuery({
    queryKey: notificationKeys.stats,
    queryFn: getNotificationStats,
    refetchInterval: 30 * 1000,
    staleTime: 20 * 1000,
  });

export const useNotifications = ({
  page = 0,
  size = 20,
  email,
  status,
  type,
  referenceId,
  referenceType,
} = {}) =>
  useQuery({
    queryKey: notificationKeys.list({ page, size, email, status, type, referenceId, referenceType }),
    queryFn: async () => {
      if (referenceId && referenceType) {
        return getNotificationsByReference({ referenceId, referenceType, page, size });
      }
      if (email) {
        return getNotificationsByEmail({ email, page, size });
      }
      if (status) {
        return getNotificationsByStatus({ status, page, size });
      }
      return getNotifications({ page, size });
    },
    select: (data) => {
      if (!type) return data;
      return {
        ...data,
        content: (data?.content || []).filter((item) => item.type === type),
      };
    },
    keepPreviousData: true,
  });

export const useNotification = (id) =>
  useQuery({
    queryKey: notificationKeys.detail(id),
    queryFn: () => getNotificationById(id),
    enabled: Boolean(id),
  });

export const useSendNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: sendNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.all });
    },
  });
};

export const useSendCustomNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: sendCustomNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.all });
    },
  });
};

export const useRetryNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: retryNotification,
    onSuccess: (data, id) => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.all });
      queryClient.invalidateQueries({ queryKey: notificationKeys.detail(id) });
    },
  });
};
