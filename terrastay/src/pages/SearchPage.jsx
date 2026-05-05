import { Suspense, lazy, useRef, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { SlidersHorizontal } from 'lucide-react';
import { useHotels } from '../hooks/useCatalogQueries';
import HeroSearchBar from '../components/search/HeroSearchBar';
import SearchFilters from '../components/search/SearchFilters';
import HotelCard, { HotelCardSkeleton } from '../components/hotel/HotelCard';
import Pagination from '../components/ui/Pagination';
import EmptyState from '../components/ui/EmptyState';
import styles from './SearchPage.module.css';

const HotelMap = lazy(() => import('../components/map/HotelMap'));

const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  // Read URL params
  const city = searchParams.get('city') || '';
  const q = searchParams.get('q') || '';
  const page = Number(searchParams.get('page') || '0'); // 0-indexed
  const minRating = searchParams.get('minRating') || '';
  const maxRating = searchParams.get('maxRating') || '';
  const amenity = searchParams.get('amenity') || '';
  const hasImage = searchParams.get('hasImage') || '';

  // Local filter state
  const [filters, setFilters] = useState({
    minRating: minRating ? Number(minRating) : 0,
    amenity: amenity || '',
    hasImage: hasImage === 'true',
  });

  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);
  const [selectedHotelId, setSelectedHotelId] = useState(null);
  const [mobileView, setMobileView] = useState('list');
  const cardRefs = useRef({});

  // Build API params — all matching /api/hotels query params
  const apiParams = {
    ...(city && { city }),
    ...(q && { q }),
    ...(minRating && { minRating }),
    ...(maxRating && { maxRating }),
    ...(amenity && { amenity }),
    ...(hasImage === 'true' && { hasImage: true }),
    page,
    size: 10,
  };

  const { data, isLoading } = useHotels(apiParams);
  const hotels = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const totalElements = data?.totalElements || 0;

  const handlePageChange = (newPage) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', String(newPage));
    setSearchParams(params);
  };

  const handleApplyFilters = () => {
    const params = new URLSearchParams(searchParams);
    if (filters.minRating > 0) params.set('minRating', String(filters.minRating));
    else params.delete('minRating');
    if (filters.amenity) params.set('amenity', filters.amenity);
    else params.delete('amenity');
    if (filters.hasImage) params.set('hasImage', 'true');
    else params.delete('hasImage');
    params.set('page', '0');
    setSearchParams(params);
  };

  const handleResetFilters = () => {
    setFilters({ minRating: 0, amenity: '', hasImage: false });
    const params = new URLSearchParams();
    if (city) params.set('city', city);
    params.set('page', '0');
    setSearchParams(params);
  };

  const handleSearch = ({ city: newCity }) => {
    const params = new URLSearchParams(searchParams);
    if (newCity) params.set('city', newCity);
    else params.delete('city');
    params.set('page', '0');
    setSearchParams(params);
  };

  const handleMarkerClick = (hotelId) => {
    setSelectedHotelId(hotelId);
    cardRefs.current[hotelId]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  };

  return (
    <div>
      <div className={styles.searchBarWrap}>
        <div className={styles.searchBarInner}>
          <HeroSearchBar compact onSearch={handleSearch} defaultValues={{ city }} />
        </div>
      </div>

      <div className={`container ${styles.content}`}>
        <div className={styles.toolbar}>
          <div className={styles.resultCount}>
            {isLoading ? 'Searching...' : (
              <span>
                <strong>{totalElements}</strong> hotel{totalElements !== 1 ? 's' : ''} found
                {city ? ` in ${city}` : ''}
              </span>
            )}
          </div>
          <button
            className={styles.mobileFilterBtn}
            onClick={() => setMobileFiltersOpen((v) => !v)}
          >
            <SlidersHorizontal size={16} /> Filters
          </button>
        </div>

        <div className={styles.mobileMapToggle}>
          <button
            className={mobileView === 'list' ? styles.toggleActive : ''}
            onClick={() => setMobileView('list')}
          >
            List
          </button>
          <button
            className={mobileView === 'map' ? styles.toggleActive : ''}
            onClick={() => setMobileView('map')}
          >
            Map ({hotels.length} hotels)
          </button>
        </div>

        <div className={styles.layout}>
          <aside className={`${styles.sidebar} ${mobileFiltersOpen ? styles.sidebarOpen : ''}`}>
            <SearchFilters
              filters={filters}
              onChange={setFilters}
              onApply={handleApplyFilters}
              onReset={handleResetFilters}
            />
          </aside>

          <div className={`${styles.results} ${mobileView === 'map' ? styles.mobileHidden : ''}`}>
            {isLoading ? (
              <div className={styles.grid}>
                {Array.from({ length: 6 }).map((_, i) => <HotelCardSkeleton key={i} />)}
              </div>
            ) : hotels.length === 0 ? (
              <EmptyState
                title="No hotels found"
                description="Try adjusting your search or filters."
                action={{ label: 'Clear Filters', onClick: handleResetFilters }}
              />
            ) : (
              <>
                <div className={styles.grid}>
                  {hotels.map((hotel) => (
                    <div
                      key={hotel.id}
                      ref={(node) => { cardRefs.current[hotel.id] = node; }}
                      className={Number(selectedHotelId) === Number(hotel.id) ? styles.selectedCard : ''}
                      onMouseEnter={() => setSelectedHotelId(hotel.id)}
                      onMouseLeave={() => setSelectedHotelId(null)}
                    >
                      <HotelCard
                        hotel={hotel}
                        onClick={() => navigate(`/hotels/${hotel.id}`)}
                      />
                    </div>
                  ))}
                </div>
                <Pagination
                  page={page}
                  totalPages={totalPages}
                  onPageChange={handlePageChange}
                />
              </>
            )}
          </div>

          <aside className={`${styles.mapPane} ${mobileView === 'list' ? styles.mobileHidden : ''}`}>
            <Suspense fallback={<div className={`skeleton ${styles.mapSkeleton}`} />}>
              <HotelMap
                hotels={hotels}
                selectedId={selectedHotelId}
                onMarkerClick={handleMarkerClick}
                height="calc(100vh - 150px)"
              />
            </Suspense>
          </aside>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
