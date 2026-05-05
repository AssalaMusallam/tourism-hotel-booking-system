import { useState } from 'react';
import { BedDouble, ChevronDown, ChevronUp } from 'lucide-react';
import AvailabilityBadge from './AvailabilityBadge';
import GuestCapacityIndicator from './GuestCapacityIndicator';
import PriceBreakdownCard from '../pricing/PriceBreakdownCard';
import PriceDisplay from '../PriceDisplay';
import Button from '../ui/Button';
import { useRoomPricePreview } from '../../hooks/usePricingRules';
import styles from './RoomTypeAvailabilityCard.module.css';

const money = (v) =>
  Number(v || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

/**
 * Card showing summary availability + pricing for one room type.
 *
 * @param {{ summary, checkIn, checkOut, requestedGuests, onSelect }} props
 */
const RoomTypeAvailabilityCard = ({ summary, checkIn, checkOut, requestedGuests, onSelect }) => {
  const [showBreakdown, setShowBreakdown] = useState(false);

  const roomTypeId = summary.roomTypeId;
  const { data: priceBreakdown, isLoading: priceLoading } = useRoomPricePreview(
    roomTypeId, checkIn, checkOut
  );

  const isBooked = !summary.available;

  return (
    <article className={`${styles.card} ${isBooked ? styles.cardBooked : ''}`}>
      {/* Image placeholder */}
      <div className={styles.imgPlaceholder}>
        <BedDouble size={32} className={styles.imgIcon} />
        {isBooked && <div className={styles.bookedOverlay}>Fully Booked</div>}
      </div>

      <div className={styles.body}>
        {/* Header row */}
        <div className={styles.header}>
          <h3 className={styles.name}>{summary.roomTypeName}</h3>
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
