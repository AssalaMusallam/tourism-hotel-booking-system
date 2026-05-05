import api from './axios';

export const waitingListKeys = {
  all: ['waiting-list'],
  mine: (page = 0) => ['waiting-list', 'mine', page],
  admin: (roomTypeId, page = 0) => ['waiting-list', 'admin', roomTypeId, page],
  count: (roomTypeId, checkIn, checkOut) => ['waiting-list', 'count', roomTypeId, checkIn, checkOut],
};

export const waitingListErrorMessage = (error) => {
  const status = error.response?.status;
  const message = error.response?.data?.message || error.response?.data?.error || '';
  const lower = message.toLowerCase();

  if (status === 409) return "You're already on the waiting list for this room.";
  if (status === 400 && (lower.includes('available units') || lower.includes('not full'))) {
    return 'Good news! This room is now available. Book it now!';
  }
  if (status === 404) return 'This waiting list entry no longer exists.';
  if (status === 403) return "You can only cancel your own waiting list entries.";
  return message || 'Waiting list request failed';
};

export const joinWaitingList = ({ roomTypeId, checkIn, checkOut }) =>
  api.post('/api/waiting-list', { roomTypeId, checkIn, checkOut }).then((response) => response.data);

export const cancelWaitingListEntry = (id) =>
  api.delete(`/api/waiting-list/${id}`).then((response) => response.data);

export const getMyWaitingList = (page = 0, size = 10) =>
  api.get('/api/waiting-list/my', { params: { page, size } }).then((response) => response.data);

export const getAdminWaitingList = (roomTypeId, page = 0, size = 20) =>
  api.get(`/api/waiting-list/admin/room-types/${roomTypeId}/waiting-list`, {
    params: { page, size },
  }).then((response) => response.data);

export const getWaitingListCount = (roomTypeId, checkIn, checkOut) =>
  api.get(`/api/waiting-list/admin/room-types/${roomTypeId}/count`, {
    params: { checkIn, checkOut },
  }).then((response) => response.data);
