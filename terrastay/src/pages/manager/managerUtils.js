export const normalizePage = (data, fallback = []) => {
  if (Array.isArray(data)) return { items: data, totalPages: 1, isMock: false };
  return {
    items: data?.content || data?.data || data?.items || fallback,
    totalPages: data?.totalPages || 1,
    isMock: false,
  };
};

export const formatDate = (value) => value ? new Date(value).toLocaleDateString('en') : '-';
export const money = (value) => `$${Number(value || 0).toLocaleString('en')}`;

export const listFromResponse = (data) => (
  Array.isArray(data) ? data : data?.content || data?.data || data?.items || []
);

export const permissionMessage = (error) => (
  error?.response?.status === 403 ? "You don't have permission to access this resource" : 'Could not load data'
);
