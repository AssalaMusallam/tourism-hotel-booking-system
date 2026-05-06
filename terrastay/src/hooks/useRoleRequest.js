import { useMemo, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';

const STORAGE_KEY = 'terrastay_role_requests';

const readRequests = () => {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const writeRequests = (requests) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(requests));
};

export const useRoleRequest = (user) => {
  const [requests, setRequests] = useState(readRequests);
  const currentRequest = useMemo(
    () => requests.find((request) => String(request.userId) === String(user?.id || user?.email)),
    [requests, user?.email, user?.id]
  );

  const requestMutation = useMutation({
    mutationFn: async () => {
      const next = {
        id: `${Date.now()}`,
        userId: user?.id || user?.email,
        name: user?.fullName || user?.name || 'Guest',
        email: user?.email,
        currentRole: user?.role || 'GUEST',
        status: 'pending',
        requestedAt: new Date().toISOString(),
      };
      const nextRequests = [
        ...requests.filter((request) => String(request.userId) !== String(next.userId)),
        next,
      ];
      writeRequests(nextRequests);
      return nextRequests;
    },
    onSuccess: (nextRequests) => {
      setRequests(nextRequests);
      toast.success('تم إرسال طلبك بنجاح');
    },
  });

  return {
    requests,
    currentRequest,
    requestRoleUpgrade: requestMutation.mutate,
    isSubmitting: requestMutation.isPending,
  };
};

export const useAdminRoleRequests = () => {
  const [requests, setRequests] = useState(readRequests);

  const updateStatus = (id, status) => {
    const next = requests.map((request) =>
      String(request.id) === String(id) ? { ...request, status } : request
    );
    writeRequests(next);
    setRequests(next);
    toast.success(status === 'approved' ? 'تم قبول الطلب' : 'تم رفض الطلب');
  };

  return { requests, updateStatus };
};

export default useRoleRequest;
