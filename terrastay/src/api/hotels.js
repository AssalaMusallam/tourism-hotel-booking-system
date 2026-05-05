import api from './axios';
import { mockHotels } from './mockData';

const simulateDelay = (ms = 500) => new Promise((r) => setTimeout(r, ms));

export const getHotels = async (params = {}) => {
  try {
    const { data } = await api.get('/hotels', { params });
    return data;
  } catch {
    await simulateDelay();
    let results = [...mockHotels];

    if (params.city) {
      results = results.filter((h) => h.city.toLowerCase() === params.city.toLowerCase());
    }
    if (params.featured) {
      results = results.filter((h) => h.featured);
    }
    if (params.minPrice) {
      results = results.filter((h) => h.pricePerNight >= Number(params.minPrice));
    }
    if (params.maxPrice) {
      results = results.filter((h) => h.pricePerNight <= Number(params.maxPrice));
    }
    if (params.stars) {
      const starsArr = Array.isArray(params.stars) ? params.stars.map(Number) : [Number(params.stars)];
      results = results.filter((h) => starsArr.includes(h.stars));
    }
    if (params.amenities) {
      const amenitiesArr = Array.isArray(params.amenities) ? params.amenities : [params.amenities];
      results = results.filter((h) => amenitiesArr.every((a) => h.amenities.includes(a)));
    }

    if (params.sort === 'price_asc') results.sort((a, b) => a.pricePerNight - b.pricePerNight);
    if (params.sort === 'price_desc') results.sort((a, b) => b.pricePerNight - a.pricePerNight);
    if (params.sort === 'rating') results.sort((a, b) => b.rating - a.rating);

    const page = Number(params.page) || 1;
    const limit = Number(params.limit) || 9;
    const start = (page - 1) * limit;
    const paginatedData = results.slice(start, start + limit);

    return { data: paginatedData, total: results.length, page, limit };
  }
};

export const getHotelById = async (id) => {
  try {
    const { data } = await api.get(`/hotels/${id}`);
    return data;
  } catch {
    await simulateDelay(300);
    const hotel = mockHotels.find((h) => h.id === id);
    if (!hotel) throw new Error('Hotel not found');
    return hotel;
  }
};

export const getHotelAvailability = async (id, params) => {
  try {
    const { data } = await api.get(`/hotels/${id}/availability`, { params });
    return data;
  } catch {
    await simulateDelay(300);
    const hotel = mockHotels.find((h) => h.id === id);
    if (!hotel) return [];
    return hotel.rooms.filter((r) => r.available);
  }
};

export const getHotelRooms = async (id) => {
  try {
    const { data } = await api.get(`/hotels/${id}/rooms`);
    return data;
  } catch {
    await simulateDelay(300);
    const hotel = mockHotels.find((h) => h.id === id);
    return hotel?.rooms || [];
  }
};

export const createHotel = async (hotelData) => {
  try {
    const { data } = await api.post('/hotels', hotelData);
    return data;
  } catch {
    await simulateDelay();
    return { ...hotelData, id: String(Date.now()), rating: 0, reviewCount: 0, rooms: [] };
  }
};

export const updateHotel = async (id, hotelData) => {
  try {
    const { data } = await api.put(`/hotels/${id}`, hotelData);
    return data;
  } catch {
    await simulateDelay();
    return { ...hotelData, id };
  }
};

export const deleteHotel = async (id) => {
  try {
    await api.delete(`/hotels/${id}`);
  } catch {
    await simulateDelay();
  }
};

export const createRoom = async (hotelId, roomData) => {
  try {
    const { data } = await api.post(`/hotels/${hotelId}/rooms`, roomData);
    return data;
  } catch {
    await simulateDelay();
    return { ...roomData, id: 'r' + Date.now() };
  }
};

export const updateRoom = async (roomId, roomData) => {
  try {
    const { data } = await api.put(`/rooms/${roomId}`, roomData);
    return data;
  } catch {
    await simulateDelay();
    return { ...roomData, id: roomId };
  }
};

export const deleteRoom = async (roomId) => {
  try {
    await api.delete(`/rooms/${roomId}`);
  } catch {
    await simulateDelay();
  }
};
