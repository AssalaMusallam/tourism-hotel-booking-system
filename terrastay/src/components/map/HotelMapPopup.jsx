const money = (value) => {
  const amount = Number(value || 0);
  if (!amount) return 'Price unavailable';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(amount);
};

export const getHotelPrice = (hotel) =>
  hotel?.minPricePerNight ?? hotel?.pricePerNight ?? hotel?.basePrice ?? hotel?.startingPrice ?? 0;

export const getHotelPopupHtml = (hotel) => {
  const rating = hotel.rating != null ? Number(hotel.rating).toFixed(1) : 'No rating';
  const price = money(getHotelPrice(hotel));
  const place = [hotel.city, hotel.country].filter(Boolean).join(', ');

  return `
    <div class="pf-map-popup">
      <strong class="pf-map-popup-title">${hotel.name || 'Hotel'}</strong>
      <span class="pf-map-popup-meta">${rating} stars · ${price}/night</span>
      <span class="pf-map-popup-place">${place || hotel.address || ''}</span>
      <button type="button" class="pf-map-popup-button" data-hotel-id="${hotel.id}">View Hotel</button>
    </div>
  `;
};
