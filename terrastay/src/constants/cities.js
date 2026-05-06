export const CITIES = [
  { value: 'Jerusalem', label: 'Jerusalem', labelAr: 'القدس', initial: 'J', color: 'var(--color-primary)' },
  { value: 'Bethlehem', label: 'Bethlehem', labelAr: 'بيت لحم', initial: 'B', color: 'var(--color-primary-light)' },
  { value: 'Nazareth', label: 'Nazareth', labelAr: 'الناصرة', initial: 'N', color: 'var(--color-accent)' },
  { value: 'Hebron', label: 'Hebron', labelAr: 'الخليل', initial: 'H', color: 'var(--color-primary-hover)' },
  { value: 'Jericho', label: 'Jericho', labelAr: 'أريحا', initial: 'J', color: 'var(--color-text-secondary)' },
  { value: 'Ramallah', label: 'Ramallah', labelAr: 'رام الله', initial: 'R', color: 'var(--color-text-muted)' },
  { value: 'Nablus', label: 'Nablus', labelAr: 'نابلس', initial: 'N', color: 'var(--color-primary-light)' },
  { value: 'Jenin', label: 'Jenin', labelAr: 'جنين', initial: 'J', color: 'var(--color-primary)' },
];

export const CITY_VALUES = CITIES.map((c) => c.value);
