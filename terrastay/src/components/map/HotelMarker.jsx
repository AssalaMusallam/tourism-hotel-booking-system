import { getHotelPrice } from './HotelMapPopup';

const money = (value) => {
  const amount = Number(value || 0);
  if (!amount) return '$--';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(amount);
};

export const createHotelMarkerIcon = (L, hotel, selected) =>
  L.divIcon({
    className: selected ? 'pf-hotel-marker pf-hotel-marker-selected' : 'pf-hotel-marker',
    html: `<span>${money(getHotelPrice(hotel))}</span>`,
    iconSize: selected ? [64, 34] : [54, 28],
    iconAnchor: selected ? [32, 34] : [27, 28],
    popupAnchor: [0, -28],
  });
