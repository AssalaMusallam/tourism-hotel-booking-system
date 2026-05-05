import api from './axios';

const BASE = '/api/v1/notifications';

const unwrap = (response) => response.data;

export const NOTIFICATION_TYPES = [
  'BOOKING_CONFIRMED',
  'BOOKING_CANCELLED',
  'BOOKING_PENDING',
  'PAYMENT_SUCCESS',
  'PAYMENT_FAILED',
  'PAYMENT_REFUNDED',
  'BOOKING_REMINDER',
  'WELCOME_EMAIL',
  'PASSWORD_RESET',
  'REVIEW_REMINDER',
  'ROOM_AVAILABLE',
  'CUSTOM',
];

export const NOTIFICATION_STATUSES = [
  'PENDING',
  'SENT',
  'FAILED',
  'RETRY_SCHEDULED',
  'PERMANENTLY_FAILED',
];

export const REFERENCE_TYPES = ['BOOKING', 'PAYMENT', 'SYSTEM'];

export const getNotifications = ({ page = 0, size = 20 } = {}) =>
  api.get(`${BASE}/`, { params: { page, size } }).then(unwrap);

export const getNotificationById = (id) =>
  api.get(`${BASE}/${id}`).then(unwrap);

export const getNotificationsByEmail = ({ email, page = 0, size = 20 }) =>
  api.get(`${BASE}/by-email`, { params: { email, page, size } }).then(unwrap);

export const getNotificationsByStatus = ({ status, page = 0, size = 20 }) =>
  api.get(`${BASE}/by-status`, { params: { status, page, size } }).then(unwrap);

export const getNotificationsByReference = ({ referenceId, referenceType, page = 0, size = 20 }) =>
  api.get(`${BASE}/by-reference`, {
    params: { referenceId, referenceType, page, size },
  }).then(unwrap);

export const getNotificationStats = () =>
  api.get(`${BASE}/stats`).then(unwrap);

export const sendNotification = (data) =>
  api.post(`${BASE}/send`, data).then(unwrap);

export const sendCustomNotification = (data) =>
  api.post(`${BASE}/send/custom`, data).then(unwrap);

export const retryNotification = (id) =>
  api.post(`${BASE}/${id}/retry`).then(unwrap);

export const canRetryNotification = (notification) =>
  ['FAILED', 'PERMANENTLY_FAILED'].includes(notification?.status) &&
  Number(notification?.retryCount || 0) < 3;
