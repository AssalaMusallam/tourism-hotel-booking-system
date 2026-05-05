import { useState, useEffect, useCallback } from 'react';
import { Filter } from 'lucide-react';
import { useHotelAvailability } from '../../hooks/useAvailability';
import RoomTypeAvailabilityCard from './RoomTypeAvailabilityCard';
import Pagination from '../ui/Pagination';
import Button from '../ui/Button';
import SectionError from '../ui/SectionError';
import { parseApiError } from '../../lib/parseApiError';
import styles from './HotelAvailabilityList.module.css';

const SORT_OPTIONS = [
  { value: 'basePrice,asc',  label: 'Price ↑' },
  { value: 'basePrice,desc', label: 'Price ↓' },
  { value: 'capacity,asc',   label: 'Capacity' },
  { value: 'name,asc',       label: 'Name' },
];

const CardSkeleton = () => (
  <div className={styles.skeleton}>
    <div className="skeleton" style={{ height: 140 }} />
    <div className={styles.skeletonBody}>
      <div className="skeleton" style={{ height: 22, width: '60%' }} />
      <div className="skeleton" style={{ height: 16, width: '40%' }} />
      <div className="skeleton" style={{ height: 16, width: '50%' }} />
      <div className="skeleton" style={{ height: 40, width: '100%' }} />
    </div>
  </div>
);

/**
 * Paginated grid of room type availability cards for a hotel.
 *
 * @param {{ hotelId, checkIn, checkOut, guests?, q?, availableOnly?, sort?, onRoomSelect }} props
 */
const HotelAvailabilityList = ({
  hotelId,
  checkIn,
  checkOut,
  guests,
  q: externalQ,
  availableOnly: externalAvailableOnly,
  sort: externalSort,
  onRoomSelect,
}) => {
  const [availableOnly, setAvailableOnly] = useState(externalAvailableOnly ?? false);
  const [sort, setSort] = useState(externalSort ?? 'basePrice,asc');
  const [page, setPage] = useState(0);

  // Sync external props into internal state when they change
  useEffect(() => {
    if (externalAvailableOnly !== undefined) setAvailableOnly(externalAvailableOnly);
  }, [externalAvailableOnly]);

  useEffect(() => {
    if (externalSort) setSort(externalSort);
  }, [externalSort]);

  // Reset to page 0 on any filter/date change
  useEffect(() => {
    setPage(0);
  }, [hotelId, checkIn, checkOut, guests, availableOnly, sort, externalQ]);

  const { data, isLoading, isError, error, refetch } = useHotelAvailability(hotelId, {
    checkIn,
    checkOut,
    guests,
    q: externalQ,
    availableOnly,
    page,
    size: 10,
    sort,
  });

  const rooms = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const totalElements = data?.totalElements || 0;

  const handleSortChange = useCallback((e) => {
    setSort(e.target.value);
    setPage(0);
  }, []);

  const handleAvailableToggle = useCallback(() => {
    setAvailableOnly((v) => !v);
    setPage(0);
  }, []);

  return (
    <div className={styles.wrap}>
      {/* ── Filter bar ── */}
      <div className={styles.filterBar}>
        <div className={styles.filterLeft}>
          <Filter size={15} className={styles.filterIcon} />
          <label className={styles.checkboxLabel}>
            <input
              type="checkbox"
              checked={availableOnly}
              onChange={handleAvailableToggle}
            />
            <span>Available only</span>
          </label>
          <div className={styles.sortWrap}>
            <span className={styles.sortLabel}>Sort:</span>
            <select className={styles.sortSelect} value={sort} onChange={handleSortChange}>
              {SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>
        </div>
        {!isLoading && (
          <span className={styles.resultCount}>
            {totalElements} room type{totalElements === 1 ? '' : 's'}
          </span>
        )}
      </div>

      {/* ── Loading ── */}
      {isLoading && (
        <div className={styles.grid}>
          {Array.from({ length: 3 }).map((_, i) => <CardSkeleton key={i} />)}
        </div>
      )}

      {/* ── Error ── */}
      {!isLoading && isError && (
        <SectionError message={parseApiError(error).message} onRetry={refetch} />
      )}

      {/* ── Empty states ── */}
      {!isLoading && !isError && rooms.length === 0 && (
        <div className={styles.emptyState}>
          {availableOnly ? (
            <>
              <p className={styles.emptyTitle}>No available rooms for these dates.</p>
              <p className={styles.emptyHint}>Try removing the &ldquo;available only&rdquo; filter.</p>
              <Button variant="secondary" size="sm" onClick={() => setAvailableOnly(false)}>
                Show all rooms
              </Button>
            </>
          ) : externalQ ? (
            <>
              <p className={styles.emptyTitle}>No rooms match &ldquo;{externalQ}&rdquo;.</p>
              <p className={styles.emptyHint}>Try a different search term.</p>
            </>
          ) : (
            <p className={styles.emptyTitle}>This hotel has no room types configured yet.</p>
          )}
        </div>
      )}

      {/* ── Cards grid ── */}
      {!isLoading && !isError && rooms.length > 0 && (
        <>
          <div className={styles.grid}>
            {rooms.map((room) => (
              <RoomTypeAvailabilityCard
                key={room.roomTypeId}
                summary={room}
                checkIn={checkIn}
                checkOut={checkOut}
                requestedGuests={guests ? Number(guests) : undefined}
                onSelect={onRoomSelect}
              />
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
};

export default HotelAvailabilityList;
