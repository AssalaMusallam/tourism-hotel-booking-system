import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { SlidersHorizontal } from 'lucide-react';
import { getHotels } from '../api/hotels';
import HeroSearchBar from '../components/search/HeroSearchBar';
import SearchFilters from '../components/search/SearchFilters';
import HotelCard, { HotelCardSkeleton } from '../components/search/HotelCard';
import Pagination from '../components/ui/Pagination';
import EmptyState from '../components/ui/EmptyState';
import Select from '../components/ui/Select';
import useSearchParamsHook from '../hooks/useSearchParams';
import styles from './SearchPage.module.css';

const SORT_OPTIONS = [
  { value: 'recommended', label: 'Recommended' },
  { value: 'price_asc', label: 'Price: Low to High' },
  { value: 'price_desc', label: 'Price: High to Low' },
  { value: 'rating', label: 'Top Rated' },
];

const SearchPage = () => {
  const { getParam, setParam, setParams } = useSearchParamsHook();
  const city = getParam('city');
  const checkIn = getParam('checkIn');
  const checkOut = getParam('checkOut');
  const guests = getParam('guests', '2');
  const page = Number(getParam('page', '1'));
  const sort = getParam('sort', 'recommended');

  const [filters, setFilters] = useState({ minPrice: 50, maxPrice: 500, stars: [], amenities: [] });
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);

  const queryParams = {
    city: city || undefined,
    checkIn: checkIn || undefined,
    checkOut: checkOut || undefined,
    guests: guests || undefined,
    page,
    limit: 9,
    sort: sort !== 'recommended' ? sort : undefined,
    minPrice: filters.minPrice !== 50 ? filters.minPrice : undefined,
    maxPrice: filters.maxPrice !== 500 ? filters.maxPrice : undefined,
    stars: filters.stars.length > 0 ? filters.stars : undefined,
    amenities: filters.amenities.length > 0 ? filters.amenities : undefined,
  };

  const { data, isLoading } = useQuery({
    queryKey: ['hotels', 'search', queryParams],
    queryFn: () => getHotels(queryParams),
    staleTime: 5 * 60 * 1000,
  });

  const hotels = data?.data || [];
  const total = data?.total || 0;
  const totalPages = Math.ceil(total / 9);

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
    setParam('page', '1');
  };

  return (
    <div>
      <div className={styles.searchBarWrap}>
        <div className={styles.searchBarInner}>
          <HeroSearchBar compact initialValues={{ city, checkIn, checkOut, guests: Number(guests) }} />
        </div>
      </div>

      <div className={`container ${styles.content}`}>
        <div className={styles.toolbar}>
          <div className={styles.resultCount}>
            {isLoading ? 'Searching...' : (
              <span>
                <strong>{total}</strong> hotel{total !== 1 ? 's' : ''} found
                {city ? ` in ${city}` : ''}
              </span>
            )}
          </div>
          <div className={styles.toolbarRight}>
            <button
              className={styles.mobileFilterBtn}
              onClick={() => setMobileFiltersOpen((v) => !v)}
            >
              <SlidersHorizontal size={16} /> Filters
            </button>
            <Select
              options={SORT_OPTIONS}
              value={sort}
              onChange={(e) => setParams({ sort: e.target.value, page: '1' })}
              containerClassName={styles.sortSelect}
              aria-label="Sort hotels"
            />
          </div>
        </div>

        <div className={styles.layout}>
          <aside className={`${styles.sidebar} ${mobileFiltersOpen ? styles.sidebarOpen : ''}`}>
            <SearchFilters filters={filters} onChange={handleFilterChange} />
          </aside>

          <div className={styles.results}>
            {isLoading ? (
              <div className={styles.grid}>
                {Array.from({ length: 6 }).map((_, i) => <HotelCardSkeleton key={i} />)}
              </div>
            ) : hotels.length === 0 ? (
              <EmptyState
                title="No hotels found"
                description="Try adjusting your search or filters. We have great hotels across all Palestinian cities."
                actionLabel="Clear Filters"
                onAction={() => {
                  setFilters({ minPrice: 50, maxPrice: 500, stars: [], amenities: [] });
                  setParams({ city: '', page: '1' });
                }}
              />
            ) : (
              <>
                <div className={styles.grid}>
                  {hotels.map((hotel, i) => (
                    <HotelCard key={hotel.id} hotel={hotel} index={i} />
                  ))}
                </div>
                <Pagination
                  page={page}
                  totalPages={totalPages}
                  onPageChange={(p) => setParam('page', String(p))}
                />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
