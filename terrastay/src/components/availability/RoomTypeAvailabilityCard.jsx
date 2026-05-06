import { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import AvailabilityBadge from './AvailabilityBadge';
import GuestCapacityIndicator from './GuestCapacityIndicator';
import PriceBreakdownCard from '../pricing/PriceBreakdownCard';
import PriceDisplay from '../PriceDisplay';
import Button from '../ui/Button';
import { useRoomPricePreview } from '../../hooks/usePricingRules';
import { useLocalizedField } from '../../hooks/useLocalizedField';
import styles from './RoomTypeAvailabilityCard.module.css';

const money = (v) =>
  Number(v || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const roomImageFallback = (room) => {
  const name = `${room?.roomTypeName || room?.name || ''} ${room?.bedType || ''}`.toLowerCase();
  if (name.includes('family') || name.includes('عائ')) return 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=600&q=80';
  if (name.includes('suite') || name.includes('جناح')) return 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&q=80';
  if (name.includes('king') || room?.bedType === 'KING') return 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600&q=80';
  return 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600&q=80';
};

/**
 * Card showing summary availability + pricing for one room type.
 *
 * @param {{ summary, checkIn, checkOut, requestedGuests, onSelect }} props
 */
const RoomTypeAvailabilityCard = ({ summary, checkIn, checkOut, requestedGuests, onSelect }) => {
  const [showBreakdown, setShowBreakdown] = useState(false);
  const lf = useLocalizedField();

  const roomTypeId = summary.roomTypeId;
  const { data: priceBreakdown, isLoading: priceLoading } = useRoomPricePreview(
    roomTypeId, checkIn, checkOut
  );

  const isBooked = !summary.available;
  const roomName = lf({ ...summary, name: summary.roomTypeName }, 'name');
  const roomImage = summary.images?.[0]?.imageUrl || summary.imageUrl || roomImageFallback(summary);

  return (
    <article className={`${styles.card} ${isBooked ? styles.cardBooked : ''}`}>
      <div className={styles.imgPlaceholder}>
        <img src={roomImage} alt={roomName} loading="lazy" className={styles.roomImage} />
        {isBooked && <div className={styles.bookedOverlay}>Fully Booked</div>}
      </div>

      <div className={styles.body}>
        {/* Header row */}
        <div className={styles.header}>
          <h3 className={styles.name}>{roomName}</h3>
          <AvailabilityBadge
            available={summary.available}
            remainingUnits={summary.remainingUnits}
          />
        </div>

        {/* Capacity */}
        <GuestCapacityIndicator
          capacity={summary.capacity}
          requestedGuests={requestedGuests}
        />

        {/* Guest capacity warning */}
        {requestedGuests !== undefined && requestedGuests > summary.capacity && (
          <p className={styles.capacityWarn}>
            This room fits max {summary.capacity} guests
          </p>
        )}

        {/* Base price */}
        <div className={styles.basePriceRow}>
          <span className={styles.priceLabel}>Base price:</span>
          <PriceDisplay usdAmount={summary.basePrice} size="sm" suffix="/night" />
        </div>

        {/* Price preview area */}
        <div className={styles.priceArea}>
          {priceLoading ? (
            <div className={styles.priceSkeleton}>
              <div className="skeleton" style={{ height: 18, width: '60%' }} />
              <div className="skeleton" style={{ height: 14, width: '40%', marginTop: 6 }} />
            </div>
          ) : priceBreakdown ? (
            <>
              <div className={styles.totalRow}>
                <span className={styles.totalLabel}>Estimated total:</span>
                <strong className={styles.totalAmount}>{money(priceBreakdown.totalPrice)}</strong>
              </div>
              <span className={styles.totalNote}>incl. taxes &amp; seasonal rules</span>
              <button
                type="button"
                className={styles.breakdownToggle}
                onClick={() => setShowBreakdown((v) => !v)}
              >
                {showBreakdown ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                {showBreakdown ? 'Hide breakdown' : 'View breakdown'}
              </button>
              {showBreakdown && (
                <div className={styles.breakdownWrap}>
                  <PriceBreakdownCard breakdown={priceBreakdown} />
                </div>
              )}
            </>
          ) : null}
        </div>

        {/* CTA */}
        <div className={styles.cta}>
          <Button
            variant="primary"
            size="md"
            disabled={isBooked || (requestedGuests !== undefined && requestedGuests > summary.capacity)}
            onClick={() => !isBooked && onSelect(roomTypeId)}
          >
            {isBooked ? 'Fully Booked' : 'Book Now →'}
          </Button>
        </div>
      </div>
    </article>
  );
};

export default RoomTypeAvailabilityCard;
