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
  'إنترنت فايبر عالي السرعة': 'High-speed WiFi',
  'موقف مجاني': 'Free Parking',
  'موقف سيارات مجاني مع خدمة فاليه': 'Free valet parking',
  مسبح: 'Pool',
  'مسبح خارجي مدفأ': 'Heated outdoor pool',
  'مركز لياقة': 'Fitness Center',
  'صالة رياضية مجهزة': 'Fitness Center',
  سبا: 'Spa',
  'مركز صحي وسبا فاخر': 'Luxury spa',
  مطعم: 'Restaurant',
  'مطعم عربي أصيل': 'Restaurant',
  'خدمة الغرف': 'Room Service',
  'خدمة غرف على مدار الساعة': 'Room Service',
  تكييف: 'Air Conditioning',
  'تكييف وتدفئة مركزي': 'Air Conditioning',
  'خدمة غسيل': 'Laundry Service',
  'خدمة غسيل وكوي': 'Laundry Service',
  'مركز أعمال': 'Business Center',
  'قاعة اجتماعات': 'Conference Room',
  'نقل من المطار': 'Airport Shuttle',
  'إطلالة حديقة': 'Garden View',
  'إطلالة بانورامية على المدينة': 'City View',
};

export function useLocalizedField() {
  const { language } = useLanguage();

  return (obj, field) => {
    if (!obj) return '';
    if (language === 'en') {
      if (obj[`${field}En`] || obj[`${field}_en`]) return obj[`${field}En`] || obj[`${field}_en`];
      if (field === 'city' || field === 'country') return cityEn[obj[field]] || (field === 'country' && obj[field] === 'فلسطين' ? 'Palestine' : obj[field] || '');
      if (field === 'name' && amenityEn[obj.name]) return amenityEn[obj.name];
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
