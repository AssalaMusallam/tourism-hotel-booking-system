const imageUrl = (photoId) =>
  `https://images.unsplash.com/photo-${photoId}?auto=format&fit=crop&w=400&q=80`;

export const FALLBACK_CITY_IMAGE = imageUrl('1500530855697-b586d89ba3ee');

export const CITY_IMAGES = {
  القدس: imageUrl('1552423314-cf29ab68ad73'),
  Jerusalem: imageUrl('1552423314-cf29ab68ad73'),
  'بيت لحم': imageUrl('1583395838144-d7e4e7b81c10'),
  Bethlehem: imageUrl('1583395838144-d7e4e7b81c10'),
  أريحا: imageUrl('1509316785289-025f5b846b35'),
  Jericho: imageUrl('1509316785289-025f5b846b35'),
  'رام الله': imageUrl('1558618666-fcd25c85cd64'),
  Ramallah: imageUrl('1558618666-fcd25c85cd64'),
  نابلس: imageUrl('1594389615184-7b163e4da729'),
  Nablus: imageUrl('1594389615184-7b163e4da729'),
  الخليل: imageUrl('1591604129939-f1efa4d9f7fa'),
  Hebron: imageUrl('1591604129939-f1efa4d9f7fa'),
  طولكرم: imageUrl('1551882547-ff40c4a49e6c'),
  Tulkarm: imageUrl('1551882547-ff40c4a49e6c'),
  جنين: imageUrl('1569949381669-ecf31ae8e613'),
  Jenin: imageUrl('1569949381669-ecf31ae8e613'),
};

export function getCityImage(cityName) {
  if (!cityName) return FALLBACK_CITY_IMAGE;

  const normalizedCity = String(cityName).trim().toLowerCase();
  const match = Object.entries(CITY_IMAGES).find(
    ([name]) => name.toLowerCase() === normalizedCity
  );

  return match?.[1] || FALLBACK_CITY_IMAGE;
}
