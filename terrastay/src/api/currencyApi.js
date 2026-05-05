import api from './axios';

export const getSupportedCurrencies = () =>
  api.get('/api/currencies').then((response) => response.data);

export const getRoomPriceInCurrency = ({ roomTypeId, checkIn, checkOut, currency }) =>
  api.get(`/api/currencies/room-types/${roomTypeId}/price`, {
    params: { checkIn, checkOut, currency },
  }).then((response) => response.data);
