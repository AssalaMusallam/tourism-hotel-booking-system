const image = (photoId) => `https://images.unsplash.com/photo-${photoId}?w=800&q=80`;

export const CITY_IMAGES = {
  القدس: image('1552832897-5c2a9176d7f2'),
  Jerusalem: image('1552832897-5c2a9176d7f2'),
  'بيت لحم': image('1544735716-392fe2489ffa'),
  Bethlehem: image('1544735716-392fe2489ffa'),
  'رام الله': image('1506905925346-21bda4d32df4'),
  Ramallah: image('1506905925346-21bda4d32df4'),
  نابلس: image('1520250497591-112f2f40a3f4'),
  Nablus: image('1520250497591-112f2f40a3f4'),
  أريحا: image('1518684079-3c830dcef090'),
  Jericho: image('1518684079-3c830dcef090'),
  الخليل: image('1464822759023-fed622ff2c3b'),
  Hebron: image('1464822759023-fed622ff2c3b'),
  جنين: image('1549880338-65ddcdfd017b'),
  Jenin: image('1549880338-65ddcdfd017b'),
  طولكرم: image('1551882547-ff40c4a49e6c'),
  Tulkarm: image('1551882547-ff40c4a49e6c'),
  قلقيلية: image('1566073771259-4a5f33f14c97'),
  Qalqilya: image('1566073771259-4a5f33f14c97'),
  طوباس: image('1571003123894-1f0594d2b5d9'),
  Tubas: image('1571003123894-1f0594d2b5d9'),
  الناصرة: image('1548690312-1f537be8ef74'),
  Nazareth: image('1548690312-1f537be8ef74'),
};

const amenityPool = [
  { id: 1, name: 'واي فاي', nameEn: 'WiFi', icon: 'WIFI' },
  { id: 2, name: 'موقف مجاني', nameEn: 'Free Parking', icon: 'FREE_PARKING' },
  { id: 3, name: 'مسبح', nameEn: 'Pool', icon: 'POOL' },
  { id: 4, name: 'مركز لياقة', nameEn: 'Fitness Center', icon: 'FITNESS_CENTER' },
  { id: 5, name: 'سبا', nameEn: 'Spa', icon: 'SPA' },
  { id: 6, name: 'مطعم', nameEn: 'Restaurant', icon: 'RESTAURANT' },
  { id: 7, name: 'خدمة الغرف', nameEn: 'Room Service', icon: 'ROOM_SERVICE' },
  { id: 8, name: 'تكييف', nameEn: 'Air Conditioning', icon: 'AIR_CONDITIONING' },
  { id: 9, name: 'خدمة غسيل', nameEn: 'Laundry', icon: 'LAUNDRY' },
  { id: 10, name: 'مركز أعمال', nameEn: 'Business Center', icon: 'BUSINESS_CENTER' },
  { id: 11, name: 'قاعة اجتماعات', nameEn: 'Conference Room', icon: 'CONFERENCE_ROOM' },
  { id: 12, name: 'نقل من المطار', nameEn: 'Airport Shuttle', icon: 'AIRPORT_SHUTTLE' },
  { id: 13, name: 'مسموح بالحيوانات', nameEn: 'Pets Allowed', icon: 'PETS_ALLOWED' },
  { id: 14, name: 'وصول للشاطئ', nameEn: 'Beach Access', icon: 'BEACH_ACCESS' },
  { id: 15, name: 'إطلالة حديقة', nameEn: 'Garden View', icon: 'GARDEN_VIEW' },
];

const descriptions = {
  ar: 'فندق فلسطيني أنيق يجمع بين الضيافة المحلية والمرافق الحديثة. يتميز بموقع مناسب لاستكشاف المدينة وتجربة ثقافتها وأسواقها ومعالمها التاريخية.',
  en: 'An elegant Palestinian hotel combining local hospitality with modern amenities. Its location makes it easy to explore the city, markets, culture, and historic landmarks.',
};

const cancellationPolicySummary = 'إلغاء مجاني حتى 3 أيام قبل الوصول';
const cancellationPolicySummaryEn = 'Free cancellation up to 3 days before arrival';

const hotelRows = [
  ['فندق النوفوتيل القدس', 'Novotel Jerusalem', 'القدس', 'Jerusalem', 31.7683, 35.2137, 4.5, 'شارع الزيتون، القدس', 'Olive Street, Jerusalem', '1552832897-5c2a9176d7f2'],
  ['فندق أمريكان كولوني', 'American Colony Hotel', 'القدس', 'Jerusalem', 31.7857, 35.2299, 4.8, 'شارع نابلس، القدس', 'Nablus Road, Jerusalem', '1548690312-1f537be8ef74'],
  ['فندق نوتردام القدس', 'Notre Dame Jerusalem Hotel', 'القدس', 'Jerusalem', 31.7806, 35.2268, 4.6, 'قرب باب الجديد، القدس', 'Near New Gate, Jerusalem', '1565591452-bb3b8b66c0a0'],
  ['فندق الملك داوود', 'King David Hotel', 'القدس', 'Jerusalem', 31.7746, 35.2176, 4.9, 'شارع الملك داوود، القدس', 'King David Street, Jerusalem', '1552832897-5c2a9176d7f2'],
  ['فندق القدس الكبير', 'Jerusalem Grand Hotel', 'القدس', 'Jerusalem', 31.7690, 35.2200, 4.3, 'وسط القدس', 'Central Jerusalem', '1548690312-1f537be8ef74'],
  ['فندق بيت الشرق', 'Orient House Hotel', 'القدس', 'Jerusalem', 31.7900, 35.2310, 4.4, 'الشيخ جراح، القدس', 'Sheikh Jarrah, Jerusalem', '1565591452-bb3b8b66c0a0'],
  ['فندق إنتركونتيننتال بيت لحم', 'Intercontinental Bethlehem', 'بيت لحم', 'Bethlehem', 31.7054, 35.2024, 4.7, 'شارع القدس، بيت لحم', 'Jerusalem Street, Bethlehem', '1544735716-392fe2489ffa'],
  ['فندق الراعي', 'Shepherd Hotel Bethlehem', 'بيت لحم', 'Bethlehem', 31.7020, 35.1980, 4.2, 'بيت ساحور، بيت لحم', 'Beit Sahour, Bethlehem', '1578662996442-48f60103fc96'],
  ['فندق النجمة بيت لحم', 'Star Hotel Bethlehem', 'بيت لحم', 'Bethlehem', 31.7063, 35.2050, 4.0, 'ساحة المهد، بيت لحم', 'Manger Square, Bethlehem', '1544735716-392fe2489ffa'],
  ['فندق كريستماس بيت لحم', 'Christmas Hotel Bethlehem', 'بيت لحم', 'Bethlehem', 31.7045, 35.2010, 4.3, 'شارع المهد، بيت لحم', 'Nativity Street, Bethlehem', '1578662996442-48f60103fc96'],
  ['فندق المهد', 'Nativity Hotel Bethlehem', 'بيت لحم', 'Bethlehem', 31.7038, 35.2066, 4.5, 'قرب كنيسة المهد، بيت لحم', 'Near Church of Nativity, Bethlehem', '1544735716-392fe2489ffa'],
  ['فندق موفنبيك رام الله', 'Mövenpick Hotel Ramallah', 'رام الله', 'Ramallah', 31.9038, 35.2034, 4.7, 'شارع الإرسال، رام الله', 'Al-Irsal Street, Ramallah', '1506905925346-21bda4d32df4'],
  ['فندق غراند بارك رام الله', 'Grand Park Hotel Ramallah', 'رام الله', 'Ramallah', 31.9010, 35.2060, 4.5, 'المصيون، رام الله', 'Al-Masyoun, Ramallah', '1571896349842-33c89424de2d'],
  ['فندق المدينة رام الله', 'City Hotel Ramallah', 'رام الله', 'Ramallah', 31.8980, 35.2080, 4.1, 'وسط المدينة، رام الله', 'City Center, Ramallah', '1506905925346-21bda4d32df4'],
  ['فندق رام الله بلازا', 'Ramallah Plaza Hotel', 'رام الله', 'Ramallah', 31.9050, 35.2020, 4.3, 'شارع ركب، رام الله', 'Rukab Street, Ramallah', '1571896349842-33c89424de2d'],
  ['فندق القصر رام الله', 'Palace Hotel Ramallah', 'رام الله', 'Ramallah', 31.9025, 35.2045, 4.4, 'الطيرة، رام الله', 'Al-Tireh, Ramallah', '1506905925346-21bda4d32df4'],
  ['فندق القصر نابلس', 'Al-Qasr Hotel Nablus', 'نابلس', 'Nablus', 32.2211, 35.2544, 4.3, 'شارع رفيديا، نابلس', 'Rafidia Street, Nablus', '1520250497591-112f2f40a3f4'],
  ['فندق الياسمين نابلس', 'Al-Yasmeen Hotel Nablus', 'نابلس', 'Nablus', 32.2190, 35.2570, 4.1, 'البلدة القديمة، نابلس', 'Old City, Nablus', '1571003123894-1f0594d2b5d9'],
  ['فندق جرش نابلس', 'Jerash Hotel Nablus', 'نابلس', 'Nablus', 32.2230, 35.2520, 3.9, 'شارع جامعة النجاح، نابلس', 'An-Najah Street, Nablus', '1520250497591-112f2f40a3f4'],
  ['فندق نابلس الدولي', 'Nablus International Hotel', 'نابلس', 'Nablus', 32.2200, 35.2555, 4.2, 'دوار الشهداء، نابلس', 'Martyrs Square, Nablus', '1571003123894-1f0594d2b5d9'],
  ['فندق البلد نابلس', 'Al-Balad Hotel Nablus', 'نابلس', 'Nablus', 32.2215, 35.2530, 3.8, 'وسط نابلس', 'Central Nablus', '1520250497591-112f2f40a3f4'],
  ['فندق إنتركونتيننتال أريحا', 'InterContinental Jericho', 'أريحا', 'Jericho', 31.8568, 35.4497, 4.6, 'تل السلطان، أريحا', 'Tell es-Sultan, Jericho', '1518684079-3c830dcef090'],
  ['فندق أوتيل أريحا', 'Otel Jericho', 'أريحا', 'Jericho', 31.8550, 35.4510, 4.0, 'شارع عمان، أريحا', 'Amman Street, Jericho', '1580674684081-a8888dc0e9e0'],
  ['منتجع أريحا الصحراوي', 'Jericho Desert Resort', 'أريحا', 'Jericho', 31.8600, 35.4480, 4.4, 'طريق البحر الميت، أريحا', 'Dead Sea Road, Jericho', '1518684079-3c830dcef090'],
  ['فندق البحر الميت أريحا', 'Dead Sea View Hotel Jericho', 'أريحا', 'Jericho', 31.8520, 35.4520, 4.2, 'إطلالة البحر الميت، أريحا', 'Dead Sea View, Jericho', '1580674684081-a8888dc0e9e0'],
  ['فندق الخليل الكبير', 'Hebron Grand Hotel', 'الخليل', 'Hebron', 31.5326, 35.0998, 4.1, 'عين سارة، الخليل', 'Ein Sarah, Hebron', '1464822759023-fed622ff2c3b'],
  ['فندق الكهف الخليل', 'Cave Hotel Hebron', 'الخليل', 'Hebron', 31.5310, 35.1020, 4.3, 'قرب الحرم الإبراهيمي، الخليل', 'Near Ibrahimi Mosque, Hebron', '1566073771259-4a5f33f14c97'],
  ['فندق إبراهيم الخليل', 'Ibrahim Hotel Hebron', 'الخليل', 'Hebron', 31.5340, 35.0980, 3.9, 'وسط الخليل', 'Central Hebron', '1464822759023-fed622ff2c3b'],
  ['فندق المدينة القديمة الخليل', 'Old City Hotel Hebron', 'الخليل', 'Hebron', 31.5320, 35.1010, 4.0, 'البلدة القديمة، الخليل', 'Old City, Hebron', '1566073771259-4a5f33f14c97'],
  ['فندق جنين الدولي', 'Jenin International Hotel', 'جنين', 'Jenin', 32.4611, 35.2956, 4.0, 'شارع الناصرة، جنين', 'Nazareth Street, Jenin', '1549880338-65ddcdfd017b'],
  ['فندق الزيتون جنين', 'Al-Zeitoun Hotel Jenin', 'جنين', 'Jenin', 32.4590, 35.2980, 3.8, 'وسط جنين', 'Central Jenin', '1551882547-ff40c4a49e6c'],
  ['فندق كرمل جنين', 'Carmel Hotel Jenin', 'جنين', 'Jenin', 32.4630, 35.2940, 3.9, 'حي الجابريات، جنين', 'Jabriyat, Jenin', '1549880338-65ddcdfd017b'],
  ['فندق طولكرم بلازا', 'Tulkarm Plaza Hotel', 'طولكرم', 'Tulkarm', 32.3104, 35.0286, 3.9, 'وسط طولكرم', 'Central Tulkarm', '1551882547-ff40c4a49e6c'],
  ['فندق الوادي طولكرم', 'Al-Wadi Hotel Tulkarm', 'طولكرم', 'Tulkarm', 32.3120, 35.0270, 3.7, 'شارع نابلس، طولكرم', 'Nablus Street, Tulkarm', '1549880338-65ddcdfd017b'],
  ['فندق النخيل طولكرم', 'Al-Nakheel Hotel Tulkarm', 'طولكرم', 'Tulkarm', 32.3090, 35.0300, 3.8, 'شارع باريس، طولكرم', 'Paris Street, Tulkarm', '1551882547-ff40c4a49e6c'],
  ['فندق قلقيلية المركزي', 'Qalqilya Central Hotel', 'قلقيلية', 'Qalqilya', 32.1886, 34.9706, 3.7, 'وسط قلقيلية', 'Central Qalqilya', '1566073771259-4a5f33f14c97'],
  ['فندق الأمل قلقيلية', 'Al-Amal Hotel Qalqilya', 'قلقيلية', 'Qalqilya', 32.1870, 34.9720, 3.6, 'شارع السوق، قلقيلية', 'Market Street, Qalqilya', '1551882547-ff40c4a49e6c'],
  ['فندق طوباس الجبلي', 'Tubas Mountain Hotel', 'طوباس', 'Tubas', 32.3209, 35.3694, 3.8, 'وسط طوباس', 'Central Tubas', '1571003123894-1f0594d2b5d9'],
  ['فندق الأردن طوباس', 'Al-Jordan Hotel Tubas', 'طوباس', 'Tubas', 32.3190, 35.3710, 3.7, 'شارع الأردن، طوباس', 'Jordan Street, Tubas', '1549880338-65ddcdfd017b'],
  ['فندق الناصرة الكبير', 'Nazareth Grand Hotel', 'الناصرة', 'Nazareth', 32.6996, 35.3035, 4.4, 'وسط الناصرة', 'Central Nazareth', '1551882547-ff40c4a49e6c'],
];

const roomTypesFor = (hotelId) => [
  { id: hotelId * 10 + 1, hotelId, name: 'غرفة قياسية', nameEn: 'Standard Room', basePrice: 80 + (hotelId % 5) * 10, capacity: 2, bedType: 'DOUBLE', totalUnits: 10, status: 'ACTIVE' },
  { id: hotelId * 10 + 2, hotelId, name: 'غرفة ديلوكس', nameEn: 'Deluxe Room', basePrice: 140 + (hotelId % 6) * 10, capacity: hotelId % 2 ? 2 : 3, bedType: 'KING', totalUnits: 6, status: 'ACTIVE' },
  { id: hotelId * 10 + 3, hotelId, name: 'جناح', nameEn: 'Suite', basePrice: 250 + (hotelId % 8) * 18, capacity: 4, bedType: 'KING', totalUnits: 3, status: 'ACTIVE' },
];

export const palestineHotels = hotelRows.map(([name, nameEn, city, cityEn, latitude, longitude, rating, address, addressEn, photoId], index) => {
  const id = index + 1;
  const amenities = amenityPool.filter((_, amenityIndex) => ((amenityIndex + id) % 3 !== 0)).slice(0, 4 + (id % 5));
  return {
    id,
    name,
    nameEn,
    address,
    addressEn,
    city,
    cityEn,
    country: 'فلسطين',
    countryEn: 'Palestine',
    description: descriptions.ar,
    descriptionEn: descriptions.en,
    rating,
    latitude,
    longitude,
    phoneNumber: `+972-2-628-${String(1000 + id).slice(-4)}`,
    email: `hotel${id}@terrastay.ps`,
    checkInTime: '14:00',
    checkOutTime: '12:00',
    status: 'ACTIVE',
    cancellationPolicySummary,
    cancellationPolicySummaryEn,
    amenities,
    amenityNames: amenities.map((amenity) => amenity.name),
    amenityNamesEn: amenities.map((amenity) => amenity.nameEn),
    images: [{ imageUrl: image(photoId), isPrimary: true }],
    roomTypes: roomTypesFor(id),
    minPricePerNight: 80 + (id % 5) * 10,
  };
});

export default palestineHotels;
