import useLanguage from './useLanguage';
import { palestineHotels } from '../data/palestineHotels';

const cityEn = {
  القدس: 'Jerusalem',
  'بيت لحم': 'Bethlehem',
  'رام الله': 'Ramallah',
  نابلس: 'Nablus',
  أريحا: 'Jericho',
  الخليل: 'Hebron',
  جنين: 'Jenin',
  طولكرم: 'Tulkarm',
  قلقيلية: 'Qalqilya',
  طوباس: 'Tubas',
  الناصرة: 'Nazareth',
  حيفا: 'Haifa',
};

const amenityEn = {
  'واي فاي': 'WiFi',
  'واي فاي مجاني': 'Free WiFi',
  'إنترنت فايبر عالي السرعة': 'High-Speed Fiber Internet',
  'موقف مجاني': 'Free Parking',
  'موقف سيارات مجاني': 'Free Parking',
  'موقف سيارات مجاني مع خدمة فاليه': 'Free Valet Parking',
  مسبح: 'Swimming Pool',
  'مسبح خارجي مدفأ': 'Heated Outdoor Pool',
  'مركز لياقة': 'Fitness Center',
  'صالة رياضية مجهزة': 'Fitness Center',
  سبا: 'Spa',
  'مركز صحي وسبا فاخر': 'Luxury Spa & Wellness Center',
  مطعم: 'Restaurant',
  'مطعم عربي أصيل': 'Authentic Arabic Restaurant',
  'خدمة الغرف': 'Room Service',
  'خدمة غرف على مدار الساعة': 'Room Service',
  تكييف: 'Air Conditioning',
  'مكيف هواء': 'Air Conditioning',
  'تكييف وتدفئة مركزي': 'Central AC & Heating',
  'خدمة غسيل': 'Laundry Service',
  'غسيل ملابس': 'Laundry Service',
  'خدمة غسيل وكوي': 'Laundry Service',
  'مركز أعمال': 'Business Center',
  'قاعة اجتماعات': 'Conference Room',
  'قاعة مؤتمرات': 'Conference Room',
  'نقل من المطار': 'Airport Shuttle',
  'نقل مطار': 'Airport Shuttle',
  'إطلالة حديقة': 'Garden View',
  'إطلالة على الحديقة': 'Garden View',
  'إطلالة بحرية': 'Sea View',
  'مصعد كهربائي': 'Elevator',
  'حديقة وفضاء خارجي': 'Garden & Outdoor Space',
  'تلفاز ذكي 4K مع Netflix': 'Smart 4K TV with Netflix',
  'NETFLIX مع 4K تلفاز ذكي': 'Smart 4K TV with Netflix',
  'مقهى سطح الفندق': 'Rooftop Café',
  'إطلالة بانورامية على المدينة': 'Panoramic City View',
  'شرفة خاصة مع إطلالة': 'Private Balcony with View',
  'خزنة إلكترونية في الغرفة': 'In-Room Electronic Safe',
  'جاكوزي خاص': 'Private Jacuzzi',
  'ميني بار متميز': 'Premium Minibar',
};

const amenityNameEn = (name = '') => {
  if (amenityEn[name]) return amenityEn[name];
  if (name.includes('إفطار')) return 'Free Breakfast Buffet';
  if (name.includes('فاليه') || name.includes('موقف سيارات')) return 'Free Valet Parking';
  if (name.includes('مصعد')) return 'Elevator';
  if (name.includes('حديقة')) return 'Garden & Outdoor Space';
  if (name.includes('مسبح') && name.includes('مدفأ')) return 'Heated Outdoor Pool';
  if (name.includes('مسبح')) return 'Swimming Pool';
  if (name.includes('تكييف')) return 'Central AC & Heating';
  if (name.includes('مطعم')) return 'Authentic Arabic Restaurant';
  if (name.includes('إنترنت') || name.includes('واي')) return 'High-Speed Fiber Internet';
  if (name.includes('سبا') || name.includes('صحي')) return 'Luxury Spa & Wellness Center';
  if (name.includes('Netflix') || name.includes('NETFLIX')) return 'Smart 4K TV with Netflix';
  if (name.includes('سطح')) return 'Rooftop Café';
  if (name.includes('بانورامية')) return 'Panoramic City View';
  if (name.includes('الغرف') || name.includes('غرف')) return 'Room Service';
  if (name.includes('غسيل')) return 'Laundry Service';
  if (name.includes('أعمال')) return 'Business Center';
  if (name.includes('مؤتمرات') || name.includes('اجتماعات')) return 'Conference Room';
  if (name.includes('مطار')) return 'Airport Shuttle';
  if (name.includes('بحر')) return 'Sea View';
  return '';
};

const roomNameEn = (name = '', bedType) => {
  if (!name) return '';
  if (name.includes('رئاسي')) return 'Presidential Suite';
  if (name.includes('ملكي')) return 'Royal Suite';
  if (name.includes('العروسين')) return 'Honeymoon Suite';
  if (name.includes('الحاج')) return 'Pilgrim Suite - Church of the Nativity';
  if (name.includes('فيلا')) return 'Private Vineyard Villa';
  if (name.includes('جناح')) return 'Suite';
  if (name.includes('عائ')) return 'Family Room';
  if (name.includes('ثلاث')) return 'Triple Family Room';
  if (name.includes('الأعمال') || name.includes('إكز')) return 'Executive Business Room';
  if (name.includes('روفتوب')) return 'Rooftop Room with Private Terrace';
  if (name.includes('سوبيريور')) return 'Superior Room';
  if (name.includes('ديلوكس')) return 'Deluxe Room';
  if (name.includes('قياس') || name.includes('كلاسيك') || name.includes('النخيل')) return 'Standard Room';
  if (bedType === 'KING') return 'King Room';
  if (bedType === 'QUEEN') return 'Queen Room';
  return 'Room Type';
};

const roomDescriptionEn = (room = {}) => {
  const name = room.name || room.roomTypeName || '';
  if (name.includes('جناح') || name.includes('فيلا')) {
    return 'A spacious premium room with refined furnishings, a comfortable sitting area, and thoughtful amenities for a memorable stay.';
  }
  if (name.includes('عائ') || name.includes('ثلاث')) {
    return 'A generous family room designed for comfort, with flexible bedding and practical space for groups or guests traveling with children.';
  }
  if (name.includes('الأعمال') || name.includes('إكز')) {
    return 'A business-focused room with a comfortable workspace, fast connectivity, and calm surroundings for productive stays.';
  }
  return 'A comfortable room with modern essentials, warm Palestinian hospitality, and a relaxed setting for your selected dates.';
};

export function useLocalizedField() {
  const { language } = useLanguage();

  return (obj, field) => {
    if (!obj) return '';
    if (language === 'en') {
      if (obj[`${field}En`] || obj[`${field}_en`]) return obj[`${field}En`] || obj[`${field}_en`];
      if (field === 'city' || field === 'country') return cityEn[obj[field]] || (field === 'country' && obj[field] === 'فلسطين' ? 'Palestine' : obj[field] || '');
      if (field === 'name' && amenityNameEn(obj.name)) return amenityNameEn(obj.name);
      if (field === 'name' && (obj.bedType || obj.roomTypeId || obj.basePrice)) return roomNameEn(obj.name || obj.roomTypeName, obj.bedType);
      if (field === 'description' && (obj.bedType || obj.roomTypeId || obj.basePrice)) return roomDescriptionEn(obj);
      if (field === 'cancellationPolicySummary' && obj[field]) return 'Free cancellation terms vary by booking. Please review the policy before confirming.';
      if (field === 'policies' && obj[field]) return 'Hotel policies apply to check-in, check-out, quiet hours, and guest conduct during the stay.';
      if (field === 'name' || field === 'address' || field === 'description' || field === 'cancellationPolicySummary') {
        const match = palestineHotels.find((hotel) => String(hotel.id) === String(obj.id) || hotel.name === obj.name);
        if (match?.[`${field}En`]) return match[`${field}En`];
      }
      return obj[field] || '';
    }
    return obj[field] || '';
  };
}

export default useLocalizedField;
