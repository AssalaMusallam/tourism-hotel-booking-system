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
import styles from './SearchPage.module.css';

const cities = ['القدس', 'بيت لحم', 'رام الله', 'نابلس', 'أريحا', 'الخليل', 'جنين', 'طولكرم', 'قلقيلية', 'طوباس'];

const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const q = searchParams.get('q') || '';
  const city = searchParams.get('city') || '';
  const page = Number(searchParams.get('page') || '0');
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);
  const [sort, setSort] = useState('rating');
  const [selectedCities, setSelectedCities] = useState(city ? [city] : []);
  const [rating, setRating] = useState(0);
  const [maxPrice, setMaxPrice] = useState(500);

  const apiParams = { ...(q && { search: q, q }), ...(city && { city }), page, size: 12 };
  const { data, isLoading, isError, refetch } = useHotels(apiParams);
  const sourceHotels = data?.content?.length ? data.content : palestineHotels;

  const hotels = useMemo(() => {
    const query = q.trim().toLowerCase();
    return sourceHotels
      .filter((hotel) => !query || hotel.name.toLowerCase().includes(query) || hotel.city.toLowerCase().includes(query))
      .filter((hotel) => selectedCities.length === 0 || selectedCities.includes(hotel.city))
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
            <strong>{hotels.length}</strong> فندق {city ? `في ${city}` : 'في فلسطين'}
          </div>
          <div className={styles.toolbarRight}>
            <select className={styles.sortSelect} value={sort} onChange={(event) => setSort(event.target.value)}>
              <option value="rating">الأعلى تقييماً</option>
              <option value="price">الأقل سعراً</option>
              <option value="newest">الأحدث</option>
            </select>
            <button className={styles.mobileFilterBtn} onClick={() => setMobileFiltersOpen((value) => !value)}>
              <SlidersHorizontal size={16} /> الفلاتر
            </button>
          </div>
        </div>

        <div className={styles.layout}>
          <aside className={`${styles.sidebar} ${mobileFiltersOpen ? styles.sidebarOpen : ''}`}>
            <div className={styles.filterPanel}>
              <h3>المدينة</h3>
              <div className={styles.checkboxGrid}>
                {cities.map((item) => (
                  <label key={item}><input type="checkbox" checked={selectedCities.includes(item)} onChange={() => toggleCity(item)} /> {item}</label>
                ))}
              </div>
              <h3>السعر</h3>
              <input type="range" min="0" max="500" value={maxPrice} onChange={(event) => setMaxPrice(Number(event.target.value))} />
              <span className={styles.rangeValue}>حتى ${maxPrice}</span>
              <h3>التقييم</h3>
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
                تعذر تحميل النتائج من الخادم، يتم عرض بيانات محلية.
                <button onClick={() => refetch()}>إعادة المحاولة</button>
              </div>
            )}
            {isLoading ? (
              <div className={styles.grid}>{Array.from({ length: 6 }).map((_, index) => <SkeletonCard key={index} />)}</div>
            ) : hotels.length === 0 ? (
              <EmptyState title="لم يتم العثور على نتائج" description="جرّب إزالة بعض الفلاتر أو البحث عن مدينة أخرى." />
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
