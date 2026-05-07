import { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { SlidersHorizontal } from 'lucide-react';
import { useHotels } from '../hooks/useCatalogQueries';
import SmartSearchBar from '../components/search/SmartSearchBar';
import SearchFilters from '../components/search/SearchFilters';
import HotelCard from '../components/hotel/HotelCard';
import Pagination from '../components/ui/Pagination';
import EmptyState from '../components/ui/EmptyState';
import SkeletonCard from '../components/ui/SkeletonCard';
import HotelsMap from '../components/map/HotelsMap';
import palestineHotels from '../data/palestineHotels';
import useLanguage from '../hooks/useLanguage';
import styles from './SearchPage.module.css';

const cities = [
  { value: 'القدس', label: 'القدس', labelEn: 'Jerusalem' },
  { value: 'بيت لحم', label: 'بيت لحم', labelEn: 'Bethlehem' },
  { value: 'رام الله', label: 'رام الله', labelEn: 'Ramallah' },
  { value: 'نابلس', label: 'نابلس', labelEn: 'Nablus' },
  { value: 'أريحا', label: 'أريحا', labelEn: 'Jericho' },
  { value: 'الخليل', label: 'الخليل', labelEn: 'Hebron' },
  { value: 'جنين', label: 'جنين', labelEn: 'Jenin' },
  { value: 'طولكرم', label: 'طولكرم', labelEn: 'Tulkarm' },
  { value: 'قلقيلية', label: 'قلقيلية', labelEn: 'Qalqilya' },
  { value: 'طوباس', label: 'طوباس', labelEn: 'Tubas' },
];

const CITY_NAME_MAP = {
  'رام الله': 'Ramallah',
  القدس: 'Jerusalem',
  'بيت لحم': 'Bethlehem',
  أريحا: 'Jericho',
  نابلس: 'Nablus',
  الخليل: 'Hebron',
  جنين: 'Jenin',
  طولكرم: 'Tulkarm',
  قلقيلية: 'Qalqilya',
  طوباس: 'Tubas',
  سلفيت: 'Salfit',
  'رام الله': 'Ramallah',
  القدس: 'Jerusalem',
  'بيت لحم': 'Bethlehem',
  أريحا: 'Jericho',
  نابلس: 'Nablus',
  الخليل: 'Hebron',
  جنين: 'Jenin',
  طولكرم: 'Tulkarm',
  قلقيلية: 'Qalqilya',
  طوباس: 'Tubas',
  سلفيت: 'Salfit',
  Ramallah: 'Ramallah',
  Jerusalem: 'Jerusalem',
  Bethlehem: 'Bethlehem',
  Jericho: 'Jericho',
  Nablus: 'Nablus',
  Hebron: 'Hebron',
  Jenin: 'Jenin',
  Tulkarm: 'Tulkarm',
  Qalqilya: 'Qalqilya',
  Tubas: 'Tubas',
};

const CITY_MAP = {
  ...Object.fromEntries(cities.map((item) => [item.value, item.labelEn])),
  ...CITY_NAME_MAP,
};

const cityLabel = (value, language) => {
  const match = cities.find((item) => item.value === value || item.labelEn === value);
  return language === 'en' ? match?.labelEn || value : match?.label || value;
};

const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { language, t } = useLanguage();
  const q = searchParams.get('q') || '';
  const city = searchParams.get('city') || '';
  const page = Number(searchParams.get('page') || '0');
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);
  const [sort, setSort] = useState('rating');
  const [selectedCities, setSelectedCities] = useState(city ? [city] : []);
  const [rating, setRating] = useState(0);
  const [maxPrice, setMaxPrice] = useState(500);

  const normalizedCity = city ? CITY_MAP[city] || city : '';
  const apiParams = { ...(q && { search: q, q }), ...(normalizedCity && { city: normalizedCity }), page, size: 12 };
  const { data, isLoading, isError, refetch } = useHotels(apiParams);
  const hotelList = Array.isArray(data) ? data : data?.content || data?.data || data?.hotels || [];
  const sourceHotels = hotelList.length ? hotelList : palestineHotels;

  const hotels = useMemo(() => {
    const query = q.trim().toLowerCase();
    return sourceHotels
      .filter((hotel) => !query || hotel.nameEn?.toLowerCase().includes(query) || hotel.cityEn?.toLowerCase().includes(query) || hotel.name?.toLowerCase().includes(query) || hotel.city?.toLowerCase().includes(query))
      .filter((hotel) => selectedCities.length === 0 || selectedCities.includes(hotel.city) || selectedCities.includes(hotel.cityEn) || selectedCities.some((selected) => CITY_MAP[selected] === hotel.city || CITY_MAP[selected] === hotel.cityEn))
      .filter((hotel) => Number(hotel.rating || 0) >= rating)
      .filter((hotel) => Number(hotel.minPricePerNight || hotel.pricePerNight || hotel.basePrice || 0) <= maxPrice)
      .sort((a, b) => {
        if (sort === 'price') return Number(a.minPricePerNight || 0) - Number(b.minPricePerNight || 0);
        if (sort === 'newest') return Number(b.id || 0) - Number(a.id || 0);
        return Number(b.rating || 0) - Number(a.rating || 0);
      });
  }, [sourceHotels, q, selectedCities, rating, maxPrice, sort]);

  const updateSearch = ({ q: nextQ, city: nextCity }) => {
    const params = new URLSearchParams();
    if (nextQ) params.set('q', nextQ);
    if (nextCity) params.set('city', nextCity);
    params.set('page', '0');
    setSelectedCities(nextCity ? [nextCity] : []);
    setSearchParams(params);
  };

  const toggleCity = (value) => {
    setSelectedCities((current) =>
      current.includes(value) ? current.filter((item) => item !== value) : [...current, value]
    );
  };

  return (
    <div>
      <div className={styles.searchBarWrap}>
        <div className={styles.searchBarInner}>
          <SmartSearchBar compact onSearch={updateSearch} defaultValues={{ q, city }} />
        </div>
      </div>

      <div className={`container ${styles.content}`}>
        <div className={styles.toolbar}>
          <div className={styles.resultCount}>
            <strong>{hotels.length}</strong> {city ? `${language === 'en' ? 'hotels in' : 'فندق في'} ${cityLabel(city, language)}` : t('hotelsInPalestine')}
          </div>
          <div className={styles.toolbarRight}>
            <select className={styles.sortSelect} value={sort} onChange={(event) => setSort(event.target.value)}>
              <option value="rating">{t('highestRated')}</option>
              <option value="price">{t('lowestPrice')}</option>
              <option value="newest">{t('newest')}</option>
            </select>
            <button className={styles.mobileFilterBtn} onClick={() => setMobileFiltersOpen((value) => !value)}>
              <SlidersHorizontal size={16} /> {t('filter')}
            </button>
          </div>
        </div>

        <div className={styles.layout}>
          <aside className={`${styles.sidebar} ${mobileFiltersOpen ? styles.sidebarOpen : ''}`}>
            <div className={styles.filterPanel}>
              <h3>{t('city')}</h3>
              <div className={styles.checkboxGrid}>
                {cities.map((item) => (
                  <label key={item.value}><input type="checkbox" checked={selectedCities.includes(item.value)} onChange={() => toggleCity(item.value)} /> {language === 'en' ? item.labelEn : item.label}</label>
                ))}
              </div>
              <h3>{t('price')}</h3>
              <input type="range" min="0" max="500" value={maxPrice} onChange={(event) => setMaxPrice(Number(event.target.value))} />
              <span className={styles.rangeValue}>{language === 'en' ? 'Up to' : 'حتى'} ${maxPrice}</span>
              <h3>{t('rating')}</h3>
              <div className={styles.ratingPills}>
                {[1, 2, 3, 4, 5].map((value) => (
                  <button key={value} className={rating === value ? styles.activePill : ''} onClick={() => setRating(rating === value ? 0 : value)}>{value} ★</button>
                ))}
              </div>
              <SearchFilters filters={{ minRating: rating, amenity: '', hasImage: false }} onChange={() => {}} onApply={() => {}} onReset={() => { setSelectedCities([]); setRating(0); setMaxPrice(500); }} />
            </div>
          </aside>

          <main className={styles.results}>
            <HotelsMap hotels={hotels} />
            {isError && (
              <div className={styles.errorBanner}>
                {language === 'en' ? 'Could not load server results. Showing local data.' : 'تعذر تحميل النتائج من الخادم، يتم عرض بيانات محلية.'}
                <button onClick={() => refetch()}>{t('retry')}</button>
              </div>
            )}
            {isLoading ? (
              <div className={styles.grid}>{Array.from({ length: 6 }).map((_, index) => <SkeletonCard key={index} />)}</div>
            ) : hotels.length === 0 ? (
              <EmptyState title={t('noResults')} description={language === 'en' ? 'Try removing filters or searching another city.' : 'جرّب إزالة بعض الفلاتر أو البحث عن مدينة أخرى.'} />
            ) : (
              <>
                <div className={styles.grid}>
                  {hotels.map((hotel, index) => <HotelCard key={hotel.id} hotel={hotel} index={index} />)}
                </div>
                {data?.totalPages > 1 && <Pagination page={page} totalPages={data.totalPages} onPageChange={(next) => { searchParams.set('page', String(next)); setSearchParams(searchParams); window.scrollTo({ top: 0, behavior: 'smooth' }); }} />}
              </>
            )}
          </main>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
