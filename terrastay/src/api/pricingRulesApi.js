import api from './axios';

export const pricingRuleKeys = {
  all: ['pricingRules'],
  list: (page, activeOnly) => ['pricingRules', 'list', page, activeOnly],
  detail: (id) => ['pricingRules', 'detail', id],
  preview: (basePrice, checkIn, checkOut) => ['pricingRules', 'preview', basePrice, checkIn, checkOut],
};

export const getAllPricingRules = (params = {}) =>
  api.get('/pricing-rules', { params }).then((r) => r.data);

export const getActivePricingRules = (params = {}) =>
  api.get('/pricing-rules/active', { params }).then((r) => r.data);

export const getPricingRule = (id) =>
  api.get(`/pricing-rules/${id}`).then((r) => r.data);

export const createPricingRule = (data) =>
  api.post('/pricing-rules', data).then((r) => r.data);

export const updatePricingRule = ({ id, data }) =>
  api.put(`/pricing-rules/${id}`, data).then((r) => r.data);

export const deletePricingRule = (id) =>
  api.delete(`/pricing-rules/${id}`).then((r) => r.data);

export const getPricePreview = ({ basePrice, checkIn, checkOut }) =>
  api.get('/pricing-rules/preview', { params: { basePrice, checkIn, checkOut } }).then((r) => r.data);

export const getRoomPricePreview = (roomTypeId, { checkIn, checkOut }) =>
  api.get(`/api/room-types/${roomTypeId}/price-preview`, { params: { checkIn, checkOut } }).then((r) => r.data);
